package com.hadzha3.goldbrain.data.gallery

import com.hadzha3.goldbrain.data.repository.IndexFailureRepository
import com.hadzha3.goldbrain.data.repository.IndexLookupRepository
import com.hadzha3.goldbrain.data.repository.MemoryIndexMaintenanceRepository
import com.hadzha3.goldbrain.data.repository.MemoryRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GalleryIndexer(
    private val repository: MemoryRepository,
    private val mediaSource: GalleryMediaSource,
    private val failureRepository:
        IndexFailureRepository,
    private val maintenanceRepository:
        MemoryIndexMaintenanceRepository,
    private val lookupRepository:
        IndexLookupRepository
) {
    data class Result(
        val indexed: Int,
        val failed: Int,
        val candidates: Int,
        val hasMore: Boolean
    )

    private val candidateBuffer =
        ArrayDeque<GalleryMediaItem>()

    private var scanOffset =
        0

    private var scanReachedEnd =
        false

    suspend fun pendingCount(): Int =
        mutex.withLock {
            resetScanState()

            var pending =
                0

            mediaSource.scanImages(
                startOffset = 0,
                pageSize =
                    LOOKUP_PAGE_SIZE
            ) { page ->
                val excluded =
                    lookupRepository
                        .excludedAmong(
                            page.map {
                                it.uri.toString()
                            }
                        )

                pending +=
                    page.count {
                        it.uri.toString() !in
                            excluded
                    }

                true
            }

            resetScanState()

            pending
        }

    suspend fun clearIndex() =
        mutex.withLock {
            resetScanState()

            repository.clearIndex()
            failureRepository.clearAll()
            maintenanceRepository
                .clearIgnored()
        }

    suspend fun ignoreAndRemove(
        uri: String
    ) =
        mutex.withLock {
            candidateBuffer
                .removeAll {
                    it.uri.toString() ==
                        uri
                }

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
            resetScanState()
            failureRepository.clearAll()
        }

    suspend fun abortScan() =
        mutex.withLock {
            resetScanState()
        }

    suspend fun indexNextBatch(
        batchSize: Int =
            DEFAULT_BATCH_SIZE
    ): Result =
        mutex.withLock {
            fillCandidateBuffer(
                required =
                    batchSize
            )

            val batch =
                ArrayList<GalleryMediaItem>(
                    batchSize
                )

            repeat(
                minOf(
                    batchSize,
                    candidateBuffer.size
                )
            ) {
                batch +=
                    candidateBuffer
                        .removeFirst()
            }

            var indexed = 0
            var failed = 0

            batch.forEach {
                    item ->
                try {
                    repository.index(
                        uri = item.uri,
                        createdAt =
                            item.createdAt,
                        source =
                            item.source
                    )

                    failureRepository
                        .clear(
                            item.uri
                                .toString()
                        )

                    indexed++
                } catch (
                    error: Exception
                ) {
                    when (
                        GalleryIndexErrorPolicy
                            .actionFor(
                                error
                            )
                    ) {
                        GalleryIndexErrorAction.CANCEL ->
                            throw error

                        GalleryIndexErrorAction
                            .STOP_FOR_PERMISSION ->
                            throw error

                        GalleryIndexErrorAction
                            .RECORD_PHOTO_FAILURE -> {
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
                }
            }

            val hasMore =
                candidateBuffer
                    .isNotEmpty() ||
                    !scanReachedEnd

            if (!hasMore) {
                resetScanState()
            }

            Result(
                indexed = indexed,
                failed = failed,
                candidates =
                    batch.size,
                hasMore =
                    hasMore
            )
        }

    private suspend fun fillCandidateBuffer(
        required: Int
    ) {
        if (
            candidateBuffer.size >=
            required ||
            scanReachedEnd
        ) {
            return
        }

        val scan =
            mediaSource.scanImages(
                startOffset =
                    scanOffset,
                pageSize =
                    LOOKUP_PAGE_SIZE
            ) { page ->
                val excluded =
                    lookupRepository
                        .excludedAmong(
                            page.map {
                                it.uri.toString()
                            }
                        )

                page
                    .asSequence()
                    .filter {
                        it.uri.toString() !in
                            excluded
                    }
                    .forEach(
                        candidateBuffer::
                            addLast
                    )

                candidateBuffer.size <
                    required
            }

        scanOffset =
            scan.nextOffset

        scanReachedEnd =
            scan.reachedEnd
    }

    private fun resetScanState() {
        candidateBuffer.clear()
        scanOffset = 0
        scanReachedEnd = false
    }

    companion object {
        const val DEFAULT_BATCH_SIZE =
            30

        private const val LOOKUP_PAGE_SIZE =
            250

        private val mutex =
            Mutex()
    }
}
