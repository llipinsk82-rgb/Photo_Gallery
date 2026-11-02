package com.inferno.gallery.data.ai

import kotlin.math.max
import kotlin.math.min

/** Completed scans stay indexed; a temporary failure is eligible for a bounded retry. */
internal object FaceScanSafety {
    const val RETRYABLE_FAILURE = -1
    const val RETRY_DELAY_MS: Long = 6 * 60 * 60 * 1000L
    private const val SAME_FACE_IOU_THRESHOLD = 0.65f

    fun shouldRetry(faceCount: Int, scannedAt: Long, now: Long): Boolean =
        faceCount == RETRYABLE_FAILURE &&
            scannedAt <= now &&
            now - scannedAt >= RETRY_DELAY_MS

    /** Prevent duplicates and preserve named persons when rescanning a photo. */
    fun isAlreadyIndexed(candidate: NormalizedFaceBox, indexed: List<NormalizedFaceBox>): Boolean {
        if (candidate.area() <= 0f) return false
        return indexed.any { previous ->
            val intersectionWidth = max(0f, min(candidate.right, previous.right) - max(candidate.left, previous.left))
            val intersectionHeight = max(0f, min(candidate.bottom, previous.bottom) - max(candidate.top, previous.top))
            val intersection = intersectionWidth * intersectionHeight
            val union = candidate.area() + previous.area() - intersection
            union > 0f && intersection / union >= SAME_FACE_IOU_THRESHOLD
        }
    }
}

internal data class NormalizedFaceBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun area(): Float = max(0f, right - left) * max(0f, bottom - top)
}
