package com.hadzha3.goldbrain.data.gallery

import com.hadzha3.goldbrain.data.repository.MemoryRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GalleryIndexer(
    private val repository: MemoryRepository,
    private val mediaSource: GalleryMediaSource
) {
    data class Result(
        val indexed: Int,
        val failed: Int,
        val candidates: Int,
        val hasMore: Boolean
    )

    suspend fun indexNextBatch(
        batchSize: Int = DEFAULT_BATCH_SIZE
    ): Result =
        mutex.withLock {
            val candidates = mediaSource.unindexedImages(
                indexedUris = repository.indexedUris(),
                limit = batchSize
            )

            var indexed = 0
            var failed = 0

            candidates.forEach { item ->
                runCatching {
                    repository.index(
                        uri = item.uri,
                        createdAt = item.createdAt
                    )
                }.onSuccess {
                    indexed++
                }.onFailure {
                    failed++
                }
            }

            Result(
                indexed = indexed,
                failed = failed,
                candidates = candidates.size,
                hasMore = candidates.size >= batchSize
            )
        }

    companion object {
        const val DEFAULT_BATCH_SIZE = 30

        private val mutex =
            Mutex()
    }
}
