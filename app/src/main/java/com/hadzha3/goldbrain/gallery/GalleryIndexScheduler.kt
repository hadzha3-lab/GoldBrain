package com.hadzha3.goldbrain.gallery

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object GalleryIndexScheduler {
    private const val ONE_TIME_NAME = "goldbrain-gallery-index-now"
    private const val PERIODIC_NAME = "goldbrain-gallery-index-periodic"

    fun startNow(context: Context) {
        val request = oneTimeRequest()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun continueSoon(context: Context) {
        val request = oneTimeRequest(
            delaySeconds = 2
        )

        WorkManager.getInstance(context).enqueue(
            request
        )
    }

    fun ensurePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<GalleryIndexWorker>(
            6,
            TimeUnit.HOURS
        )
            .setConstraints(baseConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun oneTimeRequest(
        delaySeconds: Long = 0
    ) = OneTimeWorkRequestBuilder<GalleryIndexWorker>()
        .setConstraints(baseConstraints())
        .apply {
            if (delaySeconds > 0) {
                setInitialDelay(
                    delaySeconds,
                    TimeUnit.SECONDS
                )
            }
        }
        .build()

    private fun baseConstraints(): Constraints =
        Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
}
