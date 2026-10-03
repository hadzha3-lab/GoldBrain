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
                .scanRecent(batchSize = 40)

            val output = Data.Builder()
                .putInt(KEY_INDEXED, result.indexed)
                .putInt(KEY_SKIPPED, result.skipped)
                .putInt(KEY_FAILED, result.failed)
                .build()

            if (result.failed > 0 && result.indexed == 0) {
                Result.retry()
            } else {
                Result.success(output)
            }
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        const val KEY_INDEXED = "indexed"
        const val KEY_SKIPPED = "skipped"
        const val KEY_FAILED = "failed"
    }
}
