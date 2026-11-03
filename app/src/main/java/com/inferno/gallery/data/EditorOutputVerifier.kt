package com.inferno.gallery.data

import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

/**
 * Verifies an encoded editor image after the destination stream has closed.
 *
 * Bitmap.compress() returning true is necessary but not sufficient: a provider
 * may accept writes and persist an incomplete or different payload. Never
 * publish an edited copy unless a complete read-back matches SHA-256.
 */
internal object EditorOutputVerifier {
    @Throws(IOException::class)
    fun writeAndVerify(
        destination: () -> OutputStream?,
        readBack: () -> InputStream?,
        encode: (OutputStream) -> Boolean
    ): Long {
        val encodedDigest = MessageDigest.getInstance("SHA-256")
        var expectedBytes = 0L

        (destination() ?: throw IOException("Cannot open image destination")).use { raw ->
            val digestStream = DigestOutputStream(raw, encodedDigest)
            val countingStream = object : FilterOutputStream(digestStream) {
                override fun write(value: Int) {
                    out.write(value)
                    expectedBytes++
                }

                override fun write(bytes: ByteArray, offset: Int, length: Int) {
                    out.write(bytes, offset, length)
                    expectedBytes += length
                }
            }
            if (!encode(countingStream)) {
                throw IOException("Image encoder failed")
            }
            countingStream.flush()
        }

        if (expectedBytes <= 0L) {
            throw IOException("Image encoder produced no data")
        }

        val readBackDigest = MessageDigest.getInstance("SHA-256")
        val actualBytes = (readBack() ?: throw IOException("Cannot read saved image")).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var count = 0L
            while (true) {
                val length = input.read(buffer)
                if (length < 0) break
                if (length == 0) continue
                readBackDigest.update(buffer, 0, length)
                count += length
            }
            count
        }

        if (actualBytes != expectedBytes ||
            !MessageDigest.isEqual(encodedDigest.digest(), readBackDigest.digest())
        ) {
            throw IOException("Saved image failed read-back verification")
        }
        return actualBytes
    }
}
