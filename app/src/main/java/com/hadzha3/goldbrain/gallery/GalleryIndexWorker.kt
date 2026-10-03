package com.hadzha3.goldbrain.gallery

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters

class GalleryIndexWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val result = GalleryScanner(applicationContext)
                .scanNextBatch(batchSize = 30)

            val output = Data.Builder()
                .putInt(KEY_INDEXED, result.indexed)
                .putInt(KEY_FAILED, result.failed)
                .putInt(KEY_CANDIDATES, result.candidates)
                .build()

            if (result.hasMore && result.indexed > 0) {
                GalleryIndexScheduler.continueSoon(
                    applicationContext
                )
            }

            when {
                result.failed > 0 && result.indexed == 0 ->
                    Result.failure(output)

                else ->
                    Result.success(output)
            }
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        const val KEY_INDEXED = "indexed"
        const val KEY_FAILED = "failed"
        const val KEY_CANDIDATES = "candidates"
    }
}
