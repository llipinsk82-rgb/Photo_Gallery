package com.inferno.gallery.data.ai

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class FaceScanCoordinatorTest {
    @Test
    fun simultaneousScansOfSameMediaNeverOverlap() = runBlocking {
        val active = AtomicInteger(0)
        val peak = AtomicInteger(0)

        (1..24).map {
            async(Dispatchers.Default) {
                FaceScanCoordinator.withMediaLock(101L) {
                    val now = active.incrementAndGet()
                    peak.updateAndGet { maxOf(it, now) }
                    try {
                        delay(5)
                    } finally {
                        active.decrementAndGet()
                    }
                }
            }
        }.awaitAll()

        assertEquals(1, peak.get())
        assertEquals(0, active.get())
    }

    @Test
    fun differentMediaScansCanProceedIndependently() = runBlocking {
        val firstEntered = CompletableDeferred<Unit>()
        val firstMayFinish = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()

        val first = async(Dispatchers.Default) {
            FaceScanCoordinator.withMediaLock(101L) {
                firstEntered.complete(Unit)
                firstMayFinish.await()
            }
        }
        try {
            withTimeout(2_000) { firstEntered.await() }
            val second = async(Dispatchers.Default) {
                FaceScanCoordinator.withMediaLock(102L) {
                    secondEntered.complete(Unit)
                }
            }
            withTimeout(2_000) { secondEntered.await() }
            second.await()
        } finally {
            firstMayFinish.complete(Unit)
            first.await()
        }
    }
}
