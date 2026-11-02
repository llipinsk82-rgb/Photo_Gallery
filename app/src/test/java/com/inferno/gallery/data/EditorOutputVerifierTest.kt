package com.inferno.gallery.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditorOutputVerifierTest {
    private val encoded = ByteArray(2048) { (it % 199).toByte() }

    @Test
    fun successfulSaveIsIndependentlyVerified() {
        val target = ByteArrayOutputStream()
        val count = EditorOutputVerifier.writeAndVerify(
            destination = { target },
            readBack = { ByteArrayInputStream(target.toByteArray()) },
            encode = { output -> output.write(encoded); true }
        )
        assertEquals(encoded.size.toLong(), count)
    }

    @Test
    fun missingOutputStreamFailsClosed() {
        assertThrows(IOException::class.java) {
            EditorOutputVerifier.writeAndVerify(
                destination = { null },
                readBack = { ByteArrayInputStream(encoded) },
                encode = { output -> output.write(encoded); true }
            )
        }
    }

    @Test
    fun encoderReturningFalseFailsClosed() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            EditorOutputVerifier.writeAndVerify(
                destination = { target },
                readBack = { ByteArrayInputStream(target.toByteArray()) },
                encode = { output -> output.write(encoded); false }
            )
        }
    }

    @Test
    fun emptyOutputFailsClosed() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            EditorOutputVerifier.writeAndVerify(
                destination = { target },
                readBack = { ByteArrayInputStream(target.toByteArray()) },
                encode = { true }
            )
        }
    }

    @Test
    fun truncatedReadBackFailsClosed() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            EditorOutputVerifier.writeAndVerify(
                destination = { target },
                readBack = { ByteArrayInputStream(target.toByteArray().dropLast(1).toByteArray()) },
                encode = { output -> output.write(encoded); true }
            )
        }
    }

    @Test
    fun sameSizeCorruptedReadBackFailsClosed() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            EditorOutputVerifier.writeAndVerify(
                destination = { target },
                readBack = {
                    val corrupted = target.toByteArray().also { it[5] = (it[5].toInt() xor 0xff).toByte() }
                    ByteArrayInputStream(corrupted)
                },
                encode = { output -> output.write(encoded); true }
            )
        }
    }

    @Test
    fun readBackUnavailableFailsClosed() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            EditorOutputVerifier.writeAndVerify(
                destination = { target },
                readBack = { null },
                encode = { output -> output.write(encoded); true }
            )
        }
    }
}
