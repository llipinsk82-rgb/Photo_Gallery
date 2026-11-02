package com.inferno.gallery.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticSearchScoringTest {

    @Test
    fun rejectsNoiseBelowConfidenceFloor() {
        val noiseScores = listOf(
            1L to 0.252f,
            2L to 0.241f,
            3L to 0.220f
        )
        assertTrue(SemanticSearchPolicy.filterMatches(noiseScores).isEmpty())
    }

    @Test
    fun keepsOnlyStrongMatchesWithinTighterMargin() {
        val matches = listOf(
            1L to 0.315f,
            2L to 0.301f,
            3L to 0.287f,
            4L to 0.278f,
            5L to 0.265f
        )
        val result = SemanticSearchPolicy.filterMatches(matches)
        assertEquals(listOf(1L, 2L, 3L), result.map { it.first })
    }

    @Test
    fun capsSemanticResults() {
        val scores = (1L..100L).map { it to (0.35f - (it * 0.0002f)) }
        val result = SemanticSearchPolicy.filterMatches(scores)
        assertTrue(result.size <= SemanticSearchPolicy.MAX_RESULTS)
    }

    @Test
    fun displayConfidenceDoesNotPretendWeakMatchIsHighConfidence() {
        assertEquals(50, SemanticSearchPolicy.displayConfidence(SemanticSearchPolicy.MIN_CONFIDENCE))
        assertTrue(SemanticSearchPolicy.displayConfidence(0.315f) > 70)
        assertTrue(SemanticSearchPolicy.displayConfidence(0.355f) <= 95)
    }
}
