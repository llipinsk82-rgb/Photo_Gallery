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
    fun onlyBlackServReleaseApksAreEligible() {
        assertTrue(BlackServReleasePolicy.isBlackServApk("BlackServGallery-arm64-v8a.apk"))
        assertTrue(BlackServReleasePolicy.isBlackServApk("BlackServGallery-universal.apk"))
        assertFalse(BlackServReleasePolicy.isBlackServApk("PhotonGallery-arm64.apk"))
        assertFalse(BlackServReleasePolicy.isBlackServApk("BlackServGallery-debug.apk"))
        assertFalse(BlackServReleasePolicy.isBlackServApk("BlackServGallery-artifacts.zip"))
    }
}
