package com.inferno.gallery.data.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackServReleasePolicyTest {
    @Test
    fun onlyBlackServVersionTagsAreEligibleForUpdates() {
        assertTrue(BlackServReleasePolicy.isBlackServTag("v3.0.0-blackserv.3"))
        assertTrue(BlackServReleasePolicy.isBlackServTag("3.1.0-blackserv.1"))
        assertFalse(BlackServReleasePolicy.isBlackServTag("v3.1.0"))
        assertFalse(BlackServReleasePolicy.isBlackServTag("v3.1.0-other.2"))
        assertFalse(BlackServReleasePolicy.isBlackServTag(""))
    }

    @Test
    fun newestReleaseIsPickedByNumericVersionNotApiArrayOrder() {
        val tags = listOf(
            "v3.0.0-blackserv.9",
            "v3.0.0-blackserv.10",
            "v3.0.0-blackserv.8"
        )
        assertTrue(BlackServReleasePolicy.newestTag(tags) == "v3.0.0-blackserv.10")
    }

    @Test
    fun newestReleaseIgnoresForeignTagsAndComparesBaseVersionFirst() {
        val tags = listOf(
            "v3.0.0-blackserv.99",
            "v3.0.1-blackserv.1",
            "v100.0.0-other.99",
            "v3.0.0"
        )
        assertTrue(BlackServReleasePolicy.newestTag(tags) == "v3.0.1-blackserv.1")
    }

    @Test
    fun onlyBlackServReleaseApksAreEligible() {
        assertTrue(BlackServReleasePolicy.isBlackServApk("BlackServGallery-arm64-v8a.apk"))
        assertTrue(BlackServReleasePolicy.isBlackServApk("BlackServGallery-universal.apk"))
        assertFalse(BlackServReleasePolicy.isBlackServApk("PhotonGallery-arm64.apk"))
        assertFalse(BlackServReleasePolicy.isBlackServApk("BlackServGallery-debug.apk"))
        assertFalse(BlackServReleasePolicy.isBlackServApk("BlackServGallery-artifacts.zip"))
    }
}
