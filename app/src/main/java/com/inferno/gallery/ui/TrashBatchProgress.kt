package com.inferno.gallery.ui

/** Immutable progress for sequential, user-confirmed MediaStore trash requests. */
data class TrashBatchProgress private constructor(
    val remaining: List<String>,
    val completedCount: Int
) {
    val currentBatch: List<String> get() = remaining.take(MAX_REQUEST_URIS)
    val isFinished: Boolean get() = remaining.isEmpty()

    /** Call only after the platform returns RESULT_OK for [currentBatch]. */
    fun acceptCurrentBatch(): TrashBatchProgress = copy(
        remaining = remaining.drop(currentBatch.size),
        completedCount = completedCount + currentBatch.size
    )

    companion object {
        // MediaStore.createTrashRequest enforces this limit for recent Android targets.
        const val MAX_REQUEST_URIS = 2000

        fun start(selectedUris: Collection<String>): TrashBatchProgress =
            TrashBatchProgress(selectedUris.distinct(), completedCount = 0)
    }
}
