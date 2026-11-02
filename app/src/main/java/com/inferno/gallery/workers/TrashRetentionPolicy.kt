package com.inferno.gallery.workers

/**
 * Never use the photo's DATE_MODIFIED or DATE_ADDED as its time in trash.
 * A custom retention timer may run only when a trustworthy trashedAt exists.
 *
 * Until that timestamp is actually persisted, callers must pass null
 * and leave permanent deletion to Android's MediaStore expiration.
 */
internal object TrashRetentionPolicy {
    fun isPastCustomRetention(
        trashedAtSeconds: Long?,
        nowSeconds: Long,
        retentionDays: Int
    ): Boolean {
        if (trashedAtSeconds == null || trashedAtSeconds <= 0L ||
            nowSeconds <= 0L || retentionDays <= 0 ||
            trashedAtSeconds >= nowSeconds
        ) {
            return false
        }
        val elapsedSeconds = nowSeconds - trashedAtSeconds
        return elapsedSeconds > retentionDays.toLong() * 86_400L
    }
}
