package com.hadzha3.goldbrain.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.hadzha3.goldbrain.appContainer
import com.hadzha3.goldbrain.core.permissions.GalleryAccessMode
import com.hadzha3.goldbrain.core.permissions.MediaPermissions
import com.hadzha3.goldbrain.data.gallery.GalleryIndexer
import com.hadzha3.goldbrain.data.repository.MemoryRepository
import kotlinx.coroutines.CancellationException

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

        val forceScan =
            inputData.getBoolean(
                KEY_FORCE_SCAN,
                false
            )

        return try {
            if (isNewRun) {
                val fullGalleryAccess =
                    MediaPermissions
                        .galleryAccessMode(
                            applicationContext
                        ) ==
                        GalleryAccessMode.FULL

                if (
                    !forceScan &&
                    fullGalleryAccess &&
                    container
                        .mediaStoreChangeTracker
                        .shouldSkipAutomaticScan()
                ) {
                    repositoryMaintenance(
                        container
                    )

                    statusRepository
                        .startRun(
                            total = 0
                        )

                    statusRepository
                        .markBatchFinished(
                            indexed = 0,
                            failed = 0,
                            hasMore = false
                        )

                    return Result.success(
                        emptyOutput()
                    )
                }

                if (fullGalleryAccess) {
                    container
                        .mediaStoreChangeTracker
                        .beginScan()
                } else {
                    container
                        .mediaStoreChangeTracker
                        .invalidate()
                }

                val totalPending =
                    container.galleryIndexer
                        .pendingCount()

                statusRepository.startRun(
                    total =
                        totalPending
                )

                if (
                    totalPending == 0
                ) {
                    repositoryMaintenance(
                        container
                    )

                    statusRepository
                        .markBatchFinished(
                            indexed = 0,
                            failed = 0,
                            hasMore = false
                        )

                    container
                        .mediaStoreChangeTracker
                        .markScanComplete()

                    return Result.success(
                        emptyOutput()
                    )
                }
            }

            if (isNewRun) {
                repositoryMaintenance(
                    container
                )
            }

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

            val shouldContinue =
                batch.hasMore &&
                    batch.candidates > 0

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
            } else {
                container
                    .mediaStoreChangeTracker
                    .markScanComplete()
            }

            Result.success(
                output
            )
        } catch (
            cancellation:
                CancellationException
        ) {
            throw cancellation
        } catch (_: Exception) {
            statusRepository
                .markError()

            Result.retry()
        }
    }

    private suspend fun repositoryMaintenance(
        container:
            com.hadzha3.goldbrain.di.AppContainer
    ) {
        container.memoryRepository
            .verifyAvailability(
                MemoryRepository
                    .DEFAULT_VERIFICATION_BATCH
            )
    }

    private fun emptyOutput(): Data =
        Data.Builder()
            .putInt(
                KEY_INDEXED,
                0
            )
            .putInt(
                KEY_FAILED,
                0
            )
            .putInt(
                KEY_CANDIDATES,
                0
            )
            .build()

    companion object {
        const val KEY_NEW_RUN =
            "new_run"

        const val KEY_FORCE_SCAN =
            "force_scan"

        const val KEY_INDEXED =
            "indexed"

        const val KEY_FAILED =
            "failed"

        const val KEY_CANDIDATES =
            "candidates"
    }
}
