package com.inferno.gallery.data.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceScanDiagnosticsTest {
    @Test
    fun noFacePhotoHasZeroLossAndZeroSaved() {
        val scan = FaceScanDiagnostics(totalDetected = 0)

        assertEquals(0, scan.categorizedCount)
        assertEquals(0, scan.uncategorizedCount)
        assertEquals(0, scan.count(FaceScanOutcome.SAVED))
        assertTrue(scan.summary(mediaId = 42L).contains("detected=0"))
    }

    @Test
    fun groupPhotoCountsEachFaceByItsActualOutcome() {
        val scan = FaceScanDiagnostics(totalDetected = 6)
        scan.record(FaceScanOutcome.SAVED)
        scan.record(FaceScanOutcome.TOO_SMALL)
        scan.record(FaceScanOutcome.POSE_FILTERED)
        scan.record(FaceScanOutcome.EXISTING)
        scan.record(FaceScanOutcome.ALIGNMENT_FAILED)
        scan.record(FaceScanOutcome.EMBEDDING_FAILED)

        assertEquals(6, scan.categorizedCount)
        assertEquals(0, scan.uncategorizedCount)
        for (outcome in FaceScanOutcome.entries) {
            assertEquals(1, scan.count(outcome))
        }
        val report = scan.summary(mediaId = 7L)
        assertTrue(report.contains("saved=1"))
        assertTrue(report.contains("tooSmall=1"))
        assertTrue(report.contains("embeddingFailed=1"))
    }

    @Test
    fun interruptedScanDoesNotMisreportUnprocessedFacesAsRejected() {
        val scan = FaceScanDiagnostics(totalDetected = 5)
        scan.record(FaceScanOutcome.SAVED)
        scan.record(FaceScanOutcome.EXISTING)

        assertEquals(2, scan.categorizedCount)
        assertEquals(3, scan.uncategorizedCount)
        assertTrue(scan.summary(mediaId = 13L).contains("uncategorized=3"))
    }

    @Test(expected = IllegalStateException::class)
    fun accidentallyClassifyingSameFaceTwiceFailsInsteadOfInflatingCounters() {
        val scan = FaceScanDiagnostics(totalDetected = 1)
        scan.record(FaceScanOutcome.SAVED)
        scan.record(FaceScanOutcome.SAVED)
    }
}
