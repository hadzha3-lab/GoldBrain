package com.hadzha3.goldbrain.data.repository

import android.net.Uri
import com.hadzha3.goldbrain.data.analysis.ImageAnalyzer
import com.hadzha3.goldbrain.data.local.MemoryDao
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.data.media.MediaAccessChecker
import com.hadzha3.goldbrain.domain.index.IndexTextCompactor
import com.hadzha3.goldbrain.domain.search.MemorySearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

class MemoryRepository(
    private val dao: MemoryDao,
    private val imageAnalyzer: ImageAnalyzer,
    private val searchEngine: MemorySearchEngine,
    private val mediaAccessChecker: MediaAccessChecker
) {
    private val searchRevision =
        MutableStateFlow(0L)

    fun memories(
        query: String
    ): Flow<List<MemoryEntity>> =
        if (
            query.isBlank()
        ) {
            dao.observeRecent(
                HOME_RECENT_LIMIT
            )
        } else {
            combine(
                dao.count(),
                searchRevision
            ) {
                    _,
                    revision ->
                revision
            }
                .mapLatest {
                    searchPaged(
                        query
                    )
                }
                .flowOn(
                    Dispatchers.Default
                )
        }

    fun memory(
        uri: String
    ): Flow<MemoryEntity?> =
        dao.observeByUri(uri)

    fun count(): Flow<Int> =
        dao.count()

    fun unavailableCount(): Flow<Int> =
        dao.unavailableCount()

    suspend fun index(
        uri: Uri,
        createdAt: Long =
            System.currentTimeMillis()
    ) {
        val existingNote =
            dao.userNote(
                uri.toString()
            ).orEmpty()

        val analysis =
            imageAnalyzer.analyze(uri)

        val now =
            System.currentTimeMillis()

        dao.upsert(
            MemoryEntity(
                uri = uri.toString(),
                createdAt = createdAt,
                category =
                    analysis.category,
                title =
                    analysis.title,
                ocrText =
                    IndexTextCompactor
                        .compactOcr(
                            analysis.text
                        ),
                labels =
                    IndexTextCompactor
                        .compactLabels(
                            analysis.labels
                        ),
                isAvailable = true,
                lastVerifiedAt = now,
                userNote = existingNote
            )
        )

        bumpSearchRevision()
    }

    suspend fun verifyAvailability(
        limit: Int =
            DEFAULT_VERIFICATION_BATCH
    ) = withContext(
        Dispatchers.IO
    ) {
        val now =
            System.currentTimeMillis()

        val uris =
            dao.urisForVerification(
                limit
            )

        uris.forEach { uriString ->
            val available =
                mediaAccessChecker
                    .isAvailable(
                        Uri.parse(
                            uriString
                        )
                    )

            dao.updateAvailability(
                uri = uriString,
                available =
                    available,
                verifiedAt = now
            )
        }

        if (uris.isNotEmpty()) {
            bumpSearchRevision()
        }
    }

    suspend fun updateUserNote(
        uri: String,
        note: String
    ) {
        dao.updateUserNote(
            uri = uri,
            note = note.trim()
        )

        bumpSearchRevision()
    }

    suspend fun removeMemory(
        uri: String
    ) {
        dao.deleteByUri(
            uri
        )

        bumpSearchRevision()
    }

    suspend fun clearIndex() {
        dao.clearAll()
        bumpSearchRevision()
    }

    private suspend fun searchPaged(
        query: String
    ): List<MemoryEntity> {
        var offset =
            0

        var best =
            emptyList<MemoryEntity>()

        while (true) {
            currentCoroutineContext()
                .ensureActive()

            val page =
                dao.page(
                    limit =
                        SEARCH_PAGE_SIZE,
                    offset =
                        offset
                )

            if (page.isEmpty()) {
                break
            }

            best =
                searchEngine.search(
                    best + page,
                    query
                )

            offset +=
                page.size

            if (
                page.size <
                SEARCH_PAGE_SIZE
            ) {
                break
            }
        }

        return best
    }

    private fun bumpSearchRevision() {
        searchRevision.value =
            searchRevision.value + 1L
    }

    companion object {
        const val DEFAULT_VERIFICATION_BATCH =
            25

        const val HOME_RECENT_LIMIT =
            200

        const val SEARCH_PAGE_SIZE =
            250
    }
}
