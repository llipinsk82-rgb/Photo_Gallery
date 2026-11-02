package com.inferno.gallery.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Fail-closed legacy worker. The current Room media model has no verified
 * time-of-trash field; DATE_MODIFIED is the original photo's modification date
 * and can never be used to determine when it entered the recycle bin.
 *
 * Android MediaStore manages system-trash expiration itself (DATE_EXPIRES).
 * Do not delete files, MediaStore items, or Room records here.
 */
class AutoCleanTrashWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val eligible = TrashRetentionPolicy.isPastCustomRetention(
            trashedAtSeconds = null, // No trustworthy timestamp is currently stored.
            nowSeconds = System.currentTimeMillis() / 1000L,
            retentionDays = 30
        )
        if (!eligible) {
            Log.i("AutoCleanTrashWorker", "Custom purging disabled: Android manages trash expiry")
        }
        return Result.success()
    }
}
