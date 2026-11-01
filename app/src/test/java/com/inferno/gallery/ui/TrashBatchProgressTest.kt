package com.inferno.gallery.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashBatchProgressTest {
    @Test fun emptySelectionHasNoRequest() {
        assertTrue(TrashBatchProgress.start(emptyList()).isFinished)
        assertTrue(TrashBatchProgress.start(emptyList()).currentBatch.isEmpty())
    }

    @Test fun oneItemUsesOneRequest() {
        val initial = TrashBatchProgress.start(listOf("one"))
        assertEquals(listOf("one"), initial.currentBatch)
        val accepted = initial.acceptCurrentBatch()
        assertTrue(accepted.isFinished)
        assertEquals(1, accepted.completedCount)
    }

    @Test fun exactlyTwoThousandItemsFitOneRequest() {
        val items = (1..2000).map { "uri-$it" }
        val initial = TrashBatchProgress.start(items)
        assertEquals(2000, initial.currentBatch.size)
        assertTrue(initial.acceptCurrentBatch().isFinished)
    }

    @Test fun twoThousandAndOneRequiresSeparateConfirmation() {
        val items = (1..2001).map { "uri-$it" }
        val initial = TrashBatchProgress.start(items)
        assertEquals(2000, initial.currentBatch.size)
        val afterFirst = initial.acceptCurrentBatch()
        assertFalse(afterFirst.isFinished)
        assertEquals(listOf("uri-2001"), afterFirst.currentBatch)
        assertEquals(2000, afterFirst.completedCount)
        assertEquals(2001, afterFirst.acceptCurrentBatch().completedCount)
    }

    @Test fun cancellationDoesNotMarkUnconfirmedMediaAsProcessed() {
        val initial = TrashBatchProgress.start((1..2100).map { "uri-$it" })
        val afterFirst = initial.acceptCurrentBatch()
        assertEquals(100, afterFirst.remaining.size)
        assertEquals(2000, afterFirst.completedCount)
        // No acceptCurrentBatch() call on platform RESULT_CANCELED.
        assertEquals((2001..2100).map { "uri-$it" }, afterFirst.remaining)
    }

    @Test fun duplicateUrisAreNeverSentTwice() {
        assertEquals(listOf("a", "b"), TrashBatchProgress.start(listOf("a", "a", "b")).currentBatch)
    }
}
