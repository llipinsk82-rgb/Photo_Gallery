package com.inferno.gallery.ui

/**
 * Central calibration for CLIP semantic search.
 *
 * Scores are cosine similarities between normalized text/image embeddings.
 * We intentionally bias toward precision: a gallery search should prefer
 * returning fewer good matches over filling the screen with semantic noise.
 */
object SemanticSearchPolicy {
    const val MIN_CONFIDENCE = 0.255f
    const val RELATIVE_MARGIN = 0.035f
    const val MAX_RESULTS = 60

    fun filterMatches(scored: List<Pair<Long, Float>>): List<Pair<Long, Float>> {
        val topScore = scored.firstOrNull()?.second ?: return emptyList()
        if (topScore < MIN_CONFIDENCE) return emptyList()

        val threshold = maxOf(MIN_CONFIDENCE, topScore - RELATIVE_MARGIN)
        return scored.asSequence()
            .filter { it.second >= threshold }
            .take(MAX_RESULTS)
            .toList()
    }

    /**
     * UI confidence is deliberately conservative. 50% means "just above our
     * acceptance floor", not a statistical probability.
     */
    fun displayConfidence(score: Float): Int {
        return (((score - MIN_CONFIDENCE) / (0.355f - MIN_CONFIDENCE)) * 45f + 50f)
            .coerceIn(50f, 95f)
            .toInt()
    }
}
