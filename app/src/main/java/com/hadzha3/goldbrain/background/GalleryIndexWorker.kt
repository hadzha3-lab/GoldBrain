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

            val output = Data.Builder()
                .putInt(KEY_INDEXED, result.indexed)
                .putInt(KEY_FAILED, result.failed)
                .putInt(KEY_CANDIDATES, result.candidates)
                .build()

            if (
                result.failed > 0 &&
                result.indexed == 0
            ) {
                statusRepository.markError(
                    indexed = result.indexed,
                    failed = result.failed
                )
                return@runCatching Result.failure(output)
            }

            val shouldContinue =
                result.hasMore &&
                    result.indexed > 0

            statusRepository.markFinished(
                indexed = result.indexed,
                failed = result.failed,
                hasMore = shouldContinue
            )

            if (shouldContinue) {
                GalleryIndexScheduler.continueSoon(
                    applicationContext
                )
            }

            Result.success(output)
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
