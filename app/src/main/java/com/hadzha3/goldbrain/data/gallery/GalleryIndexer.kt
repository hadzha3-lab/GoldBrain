package com.hadzha3.goldbrain.data.gallery

import com.hadzha3.goldbrain.data.repository.IndexFailureRepository
import com.hadzha3.goldbrain.data.repository.MemoryIndexMaintenanceRepository
import com.hadzha3.goldbrain.data.repository.MemoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GalleryIndexer(
    private val repository: MemoryRepository,
    private val mediaSource: GalleryMediaSource,
    private val failureRepository:
        IndexFailureRepository,
    private val maintenanceRepository:
        MemoryIndexMaintenanceRepository
) {
    data class Result(
        val indexed: Int,
        val failed: Int,
        val candidates: Int,
        val hasMore: Boolean
    )

    suspend fun pendingCount(): Int =
        mutex.withLock {
            mediaSource
                .countUnindexedImages(
                    indexedUris =
                        excludedUris()
                )
        }

    suspend fun clearIndex() =
        mutex.withLock {
            repository.clearIndex()
            failureRepository.clearAll()
            maintenanceRepository
                .clearIgnored()
        }

    suspend fun ignoreAndRemove(
        uri: String
    ) =
        mutex.withLock {
            maintenanceRepository
                .ignoreAndRemove(
                    uri
                )

            failureRepository
                .clear(
                    uri
                )
        }

    suspend fun retryFailures() =
        mutex.withLock {
            failureRepository.clearAll()
        }

    suspend fun indexNextBatch(
        batchSize: Int =
            DEFAULT_BATCH_SIZE
    ): Result =
        mutex.withLock {
            val candidates =
                mediaSource
                    .unindexedImages(
                        indexedUris =
                            excludedUris(),
                        limit =
                            batchSize
                    )

            var indexed = 0
            var failed = 0

            candidates.forEach {
                    item ->
                try {
                    repository.index(
                        uri = item.uri,
                        createdAt =
                            item.createdAt
                    )

                    failureRepository
                        .clear(
                            item.uri
                                .toString()
                        )

                    indexed++
                } catch (
                    cancellation:
                        CancellationException
                ) {
                    throw cancellation
                } catch (
                    error: Exception
                ) {
                    failureRepository
                        .recordFailure(
                            uri =
                                item.uri
                                    .toString(),
                            error =
                                error
                        )

                    failed++
                }
            }

            Result(
                indexed = indexed,
                failed = failed,
                candidates =
                    candidates.size,
                hasMore =
                    candidates.size >=
                        batchSize
            )
        }

    private suspend fun excludedUris():
        Set<String> =
        repository.indexedUris() +
            failureRepository
                .deferredUris() +
            maintenanceRepository
                .ignoredUris()

    companion object {
        const val DEFAULT_BATCH_SIZE =
            30

        private val mutex =
            Mutex()
    }
}
