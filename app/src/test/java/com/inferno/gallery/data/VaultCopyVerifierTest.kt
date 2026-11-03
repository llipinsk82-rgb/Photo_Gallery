package com.inferno.gallery.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class VaultCopyVerifierTest {
    private val sourceBytes = ByteArray(4096) { (it % 251).toByte() }

    @Test
    fun fullCopyMatchesSha256() {
        val target = ByteArrayOutputStream()
        val transferred = VaultCopyVerifier.copyAndVerify(
            source = { ByteArrayInputStream(sourceBytes) },
            destination = { target },
            readBack = { ByteArrayInputStream(target.toByteArray()) },
            expectedBytes = sourceBytes.size.toLong()
        )

        assertEquals(sourceBytes.size.toLong(), transferred)
        assertArrayEquals(sourceBytes, target.toByteArray())
    }

    @Test
    fun nullDestinationFailsClosed() {
        assertThrows(IOException::class.java) {
            VaultCopyVerifier.copyAndVerify(
                source = { ByteArrayInputStream(sourceBytes) },
                destination = { null },
                readBack = { ByteArrayInputStream(sourceBytes) },
                expectedBytes = sourceBytes.size.toLong()
            )
        }
    }

    @Test
    fun truncatedReadBackIsRejected() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            VaultCopyVerifier.copyAndVerify(
                source = { ByteArrayInputStream(sourceBytes) },
                destination = { target },
                readBack = { ByteArrayInputStream(target.toByteArray().copyOf(sourceBytes.size - 1)) },
                expectedBytes = sourceBytes.size.toLong()
            )
        }
    }

    @Test
    fun sameSizeCorruptionIsRejected() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            VaultCopyVerifier.copyAndVerify(
                source = { ByteArrayInputStream(sourceBytes) },
                destination = { target },
                readBack = {
                    val modified = target.toByteArray().also { it[123] = (it[123].toInt() xor 0xff).toByte() }
                    ByteArrayInputStream(modified)
                },
                expectedBytes = sourceBytes.size.toLong()
            )
        }
    }

    @Test
    fun shorterSourceThanExpectedIsRejected() {
        val target = ByteArrayOutputStream()
        assertThrows(IOException::class.java) {
            VaultCopyVerifier.copyAndVerify(
                source = { ByteArrayInputStream(sourceBytes.copyOf(sourceBytes.size - 1)) },
                destination = { target },
                readBack = { ByteArrayInputStream(target.toByteArray()) },
                expectedBytes = sourceBytes.size.toLong()
            )
        }
    }
}
