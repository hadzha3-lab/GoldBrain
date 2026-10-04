package com.hadzha3.goldbrain.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GalleryIndexScheduler {
    private const val ONE_TIME_NAME =
        "goldbrain-gallery-index-now"

    private const val CONTINUATION_NAME =
        "goldbrain-gallery-index-continuation"

    private const val PERIODIC_NAME =
        "goldbrain-gallery-index-periodic"

    fun startNow(
        context: Context,
        forceScan: Boolean = true
    ) {
        val manager =
            WorkManager.getInstance(context)

        manager.cancelUniqueWork(
            CONTINUATION_NAME
        )

        manager.enqueueUniqueWork(
            ONE_TIME_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest(
                isNewRun = true,
                forceScan = forceScan
            )
        )
    }

    fun continueSoon(
        context: Context,
        delaySeconds: Long
    ) {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CONTINUATION_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest(
                    delaySeconds =
                        delaySeconds,
                    isNewRun = false,
                    forceScan = false
                )
            )
    }

    fun ensurePeriodic(
        context: Context
    ) {
        val request =
            PeriodicWorkRequestBuilder<GalleryIndexWorker>(
                6,
                TimeUnit.HOURS
            )
                .setConstraints(
                    baseConstraints()
                )
                .setInputData(
                    workerInput(
                        isNewRun = true,
                        forceScan = false
                    )
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun cancelPeriodic(
        context: Context
    ) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(
                PERIODIC_NAME
            )
    }

    fun cancelActive(
        context: Context
    ) {
        val manager =
            WorkManager.getInstance(context)

        manager.cancelUniqueWork(
            ONE_TIME_NAME
        )

        manager.cancelUniqueWork(
            CONTINUATION_NAME
        )
    }

    fun cancelAll(
        context: Context
    ) {
        cancelActive(context)
        cancelPeriodic(context)
    }

    suspend fun hasActiveIndexWork(
        context: Context
    ): Boolean =
        withContext(
            Dispatchers.IO
        ) {
            val manager =
                WorkManager.getInstance(
                    context
                )

            val oneTime =
                try {
                    manager
                        .getWorkInfosForUniqueWork(
                            ONE_TIME_NAME
                        )
                        .get()
                } catch (_: Exception) {
                    return@withContext true
                }

            val continuation =
                try {
                    manager
                        .getWorkInfosForUniqueWork(
                            CONTINUATION_NAME
                        )
                        .get()
                } catch (_: Exception) {
                    return@withContext true
                }

            (oneTime + continuation)
                .any {
                    it.state ==
                        WorkInfo.State.ENQUEUED ||
                        it.state ==
                        WorkInfo.State.RUNNING ||
                        it.state ==
                        WorkInfo.State.BLOCKED
                }
        }

    private fun oneTimeRequest(
        delaySeconds: Long = 0,
        isNewRun: Boolean,
        forceScan: Boolean
    ) = OneTimeWorkRequestBuilder<GalleryIndexWorker>()
        .setConstraints(
            baseConstraints()
        )
        .setInputData(
            workerInput(
                isNewRun = isNewRun,
                forceScan = forceScan
            )
        )
        .apply {
            if (delaySeconds > 0) {
                setInitialDelay(
                    delaySeconds,
                    TimeUnit.SECONDS
                )
            }
        }
        .build()

    private fun workerInput(
        isNewRun: Boolean,
        forceScan: Boolean
    ) =
        Data.Builder()
            .putBoolean(
                GalleryIndexWorker.KEY_NEW_RUN,
                isNewRun
            )
            .putBoolean(
                GalleryIndexWorker.KEY_FORCE_SCAN,
                forceScan
            )
            .build()

    private fun baseConstraints(): Constraints =
        Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(
                NetworkType.NOT_REQUIRED
            )
            .build()
}
