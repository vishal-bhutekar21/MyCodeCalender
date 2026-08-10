package com.mycodecalendar.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class ContestSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Background sync job logic
            println("Background ContestSyncWorker executing successfully.")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

class SyncManager(private val context: Context) {

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<ContestSyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "mycodecalendar_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
