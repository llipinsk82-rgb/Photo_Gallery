package com.inferno.gallery.data

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

/**
 * Copies a media stream and verifies the completed destination by reading it back.
 *
 * A successful copyTo() only means that the source was read; it does not prove
 * the destination retained the complete bytes. Callers must close/publish the
 * destination before deleting the source, and preserve the source on failure.
 */
internal object VaultCopyVerifier {
    @Throws(IOException::class)
    fun copyAndVerify(
        source: () -> InputStream?,
        destination: () -> OutputStream?,
        readBack: () -> InputStream?,
        expectedBytes: Long
    ): Long {
        if (expectedBytes <= 0L) {
            throw IOException("Cannot verify a media file with an unknown or empty size")
        }

        val sourceDigest = MessageDigest.getInstance("SHA-256")
        val copiedBytes = (source() ?: throw IOException("Source stream is unavailable")).use { input ->
            (destination() ?: throw IOException("Destination stream is unavailable")).use { output ->
                val hashingOutput = DigestOutputStream(output, sourceDigest)
                val count = input.copyTo(hashingOutput)
                hashingOutput.flush()
                count
            }
        }
        if (copiedBytes != expectedBytes) {
            throw IOException("Media copy incomplete: expected $expectedBytes bytes, got $copiedBytes")
        }

        val destinationDigest = MessageDigest.getInstance("SHA-256")
        val readBytes = (readBack() ?: throw IOException("Cannot read back the copied media")).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var count = 0L
            while (true) {
                val length = input.read(buffer)
                if (length < 0) break
                if (length == 0) continue
                destinationDigest.update(buffer, 0, length)
                count += length
            }
            count
        }

        if (readBytes != copiedBytes ||
            !MessageDigest.isEqual(sourceDigest.digest(), destinationDigest.digest())
        ) {
            throw IOException("Destination media differs from the source")
        }
        return copiedBytes
    }
}
