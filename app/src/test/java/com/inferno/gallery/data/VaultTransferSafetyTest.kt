package com.inferno.gallery.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class VaultTransferSafetyTest {
    @Test
    fun failedCopyNeverPublishesOrDeletesTheSource() {
        val calls = mutableListOf<String>()
        assertThrows(IOException::class.java) {
            VaultTransferSafety.commitCopy(
                verifiedCopy = { calls += "verify"; throw IOException("Incomplete destination") },
                commitDestination = { calls += "publish"; true },
                discardSource = { calls += "delete"; true }
            )
        }
        assertEquals(listOf("verify"), calls)
    }

    @Test
    fun failedCommitNeverDeletesTheSource() {
        val calls = mutableListOf<String>()
        assertThrows(IOException::class.java) {
            VaultTransferSafety.commitCopy(
                verifiedCopy = { calls += "verify" },
                commitDestination = { calls += "publish"; false },
                discardSource = { calls += "delete"; true }
            )
        }
        assertEquals(listOf("verify", "publish"), calls)
    }

    @Test
    fun deletionOnlyFollowsVerifiedAndPublishedCopy() {
        val calls = mutableListOf<String>()
        val result = VaultTransferSafety.commitCopy(
            verifiedCopy = { calls += "verify" },
            commitDestination = { calls += "publish"; true },
            discardSource = { calls += "delete"; true }
        )
        assertTrue(result)
        assertEquals(listOf("verify", "publish", "delete"), calls)
    }

    @Test
    fun failedDeletionDoesNotLieAboutTheOutcome() {
        val result = VaultTransferSafety.commitCopy(
            verifiedCopy = {},
            commitDestination = { true },
            discardSource = { false }
        )
        assertFalse(result)
    }
}
