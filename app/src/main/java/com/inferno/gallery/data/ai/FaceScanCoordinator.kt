package com.inferno.gallery.data.ai

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Bounds synchronization memory while making every scan of the same media
 * mutually exclusive across Worker and the photo-details screen.
 *
 * Colliding media IDs may briefly serialize even when their photos differ,
 * but unrelated stripes continue in parallel.
 */
internal object FaceScanCoordinator {
    private const val STRIPE_COUNT = 64
    private val locks = Array(STRIPE_COUNT) { Mutex() }

    suspend fun <T> withMediaLock(mediaId: Long, action: suspend () -> T): T {
        val slot = (mediaId.hashCode() and Int.MAX_VALUE) % STRIPE_COUNT
        return locks[slot].withLock { action() }
    }
}
