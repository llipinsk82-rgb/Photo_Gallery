package com.inferno.gallery.data.ai

/**
 * Pure, privacy-preserving accounting for one ML Kit face scan. The counters
 * describe pipeline outcomes only; no images, names, crops or embeddings.
 */
internal enum class FaceScanOutcome(val logKey: String) {
    TOO_SMALL("tooSmall"),
    POSE_FILTERED("poseFiltered"),
    EXISTING("existing"),
    ALIGNMENT_FAILED("alignmentFailed"),
    EMBEDDING_FAILED("embeddingFailed"),
    SAVED("saved")
}

internal class FaceScanDiagnostics(val totalDetected: Int) {
    init {
        require(totalDetected >= 0) { "Detected face count cannot be negative" }
    }

    private val counters = IntArray(FaceScanOutcome.entries.size)

    val categorizedCount: Int
        get() = counters.sum()

    val uncategorizedCount: Int
        get() = (totalDetected - categorizedCount).coerceAtLeast(0)

    fun record(outcome: FaceScanOutcome) {
        check(categorizedCount < totalDetected) { "Cannot categorize more faces than ML Kit detected" }
        counters[outcome.ordinal]++
    }

    fun count(outcome: FaceScanOutcome): Int = counters[outcome.ordinal]

    fun summary(mediaId: Long): String = buildString {
        append("Face scan mediaId=").append(mediaId)
        append(" detected=").append(totalDetected)
        for (outcome in FaceScanOutcome.entries) {
            append(" ").append(outcome.logKey).append("=").append(count(outcome))
        }
        append(" uncategorized=").append(uncategorizedCount)
    }
}
