package com.hadzha3.goldbrain.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.hadzha3.goldbrain.appContainer
import com.hadzha3.goldbrain.data.gallery.GalleryIndexer
import com.hadzha3.goldbrain.data.repository.MemoryRepository

class GalleryIndexWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val container =
            applicationContext.appContainer

        val statusRepository =
            container.indexStatusRepository

        return runCatching {
            statusRepository.markRunning()

            container.memoryRepository
                .verifyAvailability(
                    MemoryRepository.DEFAULT_VERIFICATION_BATCH
                )

            val result =
                container.galleryIndexer
                    .indexNextBatch(
                        GalleryIndexer.DEFAULT_BATCH_SIZE
                    )

            statusRepository.markFinished(
                indexed = result.indexed,
                failed = result.failed,
                hasMore = result.hasMore &&
                    result.indexed > 0
            )

            val output = Data.Builder()
                .putInt(KEY_INDEXED, result.indexed)
                .putInt(KEY_FAILED, result.failed)
                .putInt(KEY_CANDIDATES, result.candidates)
                .build()

            if (
                result.hasMore &&
                result.indexed > 0
            ) {
                GalleryIndexScheduler.continueSoon(
                    applicationContext
                )
            }

            if (
                result.failed > 0 &&
                result.indexed == 0
            ) {
                Result.failure(output)
            } else {
                Result.success(output)
            }
        }.getOrElse {
            statusRepository.markError()
            Result.retry()
        }
    }

    companion object {
        const val KEY_INDEXED = "indexed"
        const val KEY_FAILED = "failed"
        const val KEY_CANDIDATES = "candidates"
    }
}
