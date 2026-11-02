package com.inferno.gallery.data.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceScanSafetyTest {
    @Test
    fun `failed scan retries after cooldown without looping immediately`() {
        val now = 20_000_000L
        val recent = now - FaceScanSafety.RETRY_DELAY_MS + 1
        val expired = now - FaceScanSafety.RETRY_DELAY_MS - 1

        assertFalse(FaceScanSafety.shouldRetry(FaceScanSafety.RETRYABLE_FAILURE, recent, now))
        assertTrue(FaceScanSafety.shouldRetry(FaceScanSafety.RETRYABLE_FAILURE, expired, now))
        assertFalse(FaceScanSafety.shouldRetry(0, expired, now))
        assertFalse(FaceScanSafety.shouldRetry(3, expired, now))
    }

    @Test
    fun `rescan recognizes already indexed face despite small bounding box jitter`() {
        val original = NormalizedFaceBox(0.1f, 0.2f, 0.3f, 0.5f)
        val shifted = NormalizedFaceBox(0.105f, 0.205f, 0.305f, 0.505f)

        assertTrue(FaceScanSafety.isAlreadyIndexed(shifted, listOf(original)))
    }

    @Test
    fun `distinct people in group photo must not be treated as duplicates`() {
        val first = NormalizedFaceBox(0.1f, 0.2f, 0.3f, 0.5f)
        val second = NormalizedFaceBox(0.32f, 0.2f, 0.52f, 0.5f)

        assertFalse(FaceScanSafety.isAlreadyIndexed(second, listOf(first)))
        assertFalse(FaceScanSafety.isAlreadyIndexed(first, emptyList()))
    }

    @Test
    fun `invalid bounds cannot suppress newly detected faces`() {
        val old = NormalizedFaceBox(0.1f, 0.1f, 0.3f, 0.3f)
        val invalid = NormalizedFaceBox(0.5f, 0.5f, 0.5f, 0.7f)
        assertFalse(FaceScanSafety.isAlreadyIndexed(invalid, listOf(old)))
    }
}
