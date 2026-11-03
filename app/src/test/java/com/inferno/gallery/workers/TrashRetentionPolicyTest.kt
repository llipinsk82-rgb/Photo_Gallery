package com.inferno.gallery.workers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A photo's last-modified time is NOT its time in the recycle bin.
 * The app may only run custom retention if it can prove a real trash timestamp.
 */
class TrashRetentionPolicyTest {
    @Test
    fun missingTrashTimestampNeverQualifiesForPermanentDeletion() {
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(null, 2_000_000L, 7))
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(null, 2_000_000L, 14))
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(null, 2_000_000L, 30))
    }

    @Test
    fun ancientDateModifiedCannotSubstituteForTrashTimestamp() {
        // Even a 20-year-old photo is safe if it was just trashed:
        // only the actual trashedAt timestamp may be passed to the policy.
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(1_999_990L, 2_000_000L, 7))
    }

    @Test
    fun expiredTrashWithVerifiedTimestampQualifies() {
        val now = 2_000_000L
        val verifiedTrashTime = now - 8 * 86_400L
        assertTrue(TrashRetentionPolicy.isPastCustomRetention(verifiedTrashTime, now, 7))
    }

    @Test
    fun boundaryIsNotExpiredEarly() {
        val now = 2_000_000L
        val cutoff = now - 7 * 86_400L
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(cutoff, now, 7))
    }

    @Test
    fun invalidTimestampsOrRetentionNeverQualify() {
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(0L, 2_000_000L, 7))
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(-1L, 2_000_000L, 7))
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(1_000_000L, 2_000_000L, 0))
        assertFalse(TrashRetentionPolicy.isPastCustomRetention(1_000_000L, 2_000_000L, -1))
    }
}
