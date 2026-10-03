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
) : CoroutineWorker(
    appContext,
    params
) {
    override suspend fun doWork(): Result {
        val container =
            applicationContext.appContainer

        val statusRepository =
            container.indexStatusRepository

        val isNewRun =
            inputData.getBoolean(
                KEY_NEW_RUN,
                true
            )

        return try {
            if (isNewRun) {
                val totalPending =
                    container.galleryIndexer
                        .pendingCount()

                statusRepository.startRun(
                    total =
                        totalPending
                )
            }

            container.memoryRepository
                .verifyAvailability(
                    MemoryRepository
                        .DEFAULT_VERIFICATION_BATCH
                )

            val batch =
                container.galleryIndexer
                    .indexNextBatch(
                        GalleryIndexer
                            .DEFAULT_BATCH_SIZE
                    )

            val output =
                Data.Builder()
                    .putInt(
                        KEY_INDEXED,
                        batch.indexed
                    )
                    .putInt(
                        KEY_FAILED,
                        batch.failed
                    )
                    .putInt(
                        KEY_CANDIDATES,
                        batch.candidates
                    )
                    .build()

            if (
                batch.failed > 0 &&
                batch.indexed == 0
            ) {
                statusRepository
                    .markError(
                        indexed =
                            batch.indexed,
                        failed =
                            batch.failed
                    )

                Result.failure(
                    output
                )
            } else {
                val shouldContinue =
                    batch.hasMore &&
                        batch.indexed > 0

                statusRepository
                    .markBatchFinished(
                        indexed =
                            batch.indexed,
                        failed =
                            batch.failed,
                        hasMore =
                            shouldContinue
                    )

                if (shouldContinue) {
                    GalleryIndexScheduler
                        .continueSoon(
                            applicationContext
                        )
                }

                Result.success(
                    output
                )
            }
        } catch (_: Throwable) {
            statusRepository
                .markError()

            Result.retry()
        }
    }

    companion object {
        const val KEY_NEW_RUN =
            "new_run"

        const val KEY_INDEXED =
            "indexed"

        const val KEY_FAILED =
            "failed"

        const val KEY_CANDIDATES =
            "candidates"
    }
}
