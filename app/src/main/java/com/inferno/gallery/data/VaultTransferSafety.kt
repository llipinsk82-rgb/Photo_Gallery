package com.inferno.gallery.data

import java.io.IOException

/**
 * The last recoverable copy must never be discarded before its replacement
 * has been independently verified and published/persisted.
 *
 * If source cleanup returns false, both copies remain for safe recovery.
 */
internal object VaultTransferSafety {
    @Throws(IOException::class)
    fun commitCopy(
        verifiedCopy: () -> Unit,
        commitDestination: () -> Boolean,
        discardSource: () -> Boolean
    ): Boolean {
        verifiedCopy()
        if (!commitDestination()) {
            throw IOException("The verified destination could not be committed")
        }
        return discardSource()
    }
    /**
     * Database writes for Vault are suspend functions. Keep the same strict
     * ordering without blocking a coroutine thread to persist Room state.
     */
    @Throws(IOException::class)
    suspend fun commitCopySuspending(
        verifiedCopy: () -> Unit,
        commitDestination: suspend () -> Boolean,
        discardSource: () -> Boolean
    ): Boolean {
        verifiedCopy()
        if (!commitDestination()) {
            throw IOException("The verified destination could not be persisted")
        }
        return discardSource()
    }

}
