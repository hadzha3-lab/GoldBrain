package com.hadzha3.goldbrain.data.repository

import android.net.Uri
import com.hadzha3.goldbrain.data.analysis.ImageAnalyzer
import com.hadzha3.goldbrain.data.local.MemoryDao
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.data.media.MediaAccessChecker
import com.hadzha3.goldbrain.domain.index.IndexTextCompactor
import com.hadzha3.goldbrain.domain.search.MemorySearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MemoryRepository(
    private val dao: MemoryDao,
    private val imageAnalyzer: ImageAnalyzer,
    private val searchEngine: MemorySearchEngine,
    private val mediaAccessChecker: MediaAccessChecker
) {
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
            dao.observeAll()
                .map { items ->
                    searchEngine.search(
                        items,
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

    suspend fun indexedUris(): Set<String> =
        dao.allUris().toHashSet()

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
    }

    suspend fun verifyAvailability(
        limit: Int =
            DEFAULT_VERIFICATION_BATCH
    ) = withContext(
        Dispatchers.IO
    ) {
        val now =
            System.currentTimeMillis()

        dao.urisForVerification(limit)
            .forEach { uriString ->
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
    }

    suspend fun updateUserNote(
        uri: String,
        note: String
    ) {
        dao.updateUserNote(
            uri = uri,
            note = note.trim()
        )
    }

    suspend fun removeMemory(
        uri: String
    ) {
        dao.deleteByUri(
            uri
        )
    }

    suspend fun clearIndex() {
        dao.clearAll()
    }

    companion object {
        const val DEFAULT_VERIFICATION_BATCH =
            25

        const val HOME_RECENT_LIMIT =
            200
    }
}
