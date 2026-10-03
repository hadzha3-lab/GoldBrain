package com.hadzha3.goldbrain.data.repository

import android.net.Uri
import com.hadzha3.goldbrain.data.analysis.ImageAnalyzer
import com.hadzha3.goldbrain.data.local.MemoryDao
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.data.media.MediaAccessChecker
import com.hadzha3.goldbrain.domain.search.MemorySearchEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepository(
    private val dao: MemoryDao,
    private val imageAnalyzer: ImageAnalyzer,
    private val searchEngine: MemorySearchEngine,
    private val mediaAccessChecker: MediaAccessChecker
) {
    fun memories(
        query: String
    ): Flow<List<MemoryEntity>> =
        dao.observeAll().map { items ->
            searchEngine.search(
                items,
                query
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
        val analysis =
            imageAnalyzer.analyze(uri)

        val searchableText =
            listOf(
                analysis.title,
                analysis.category,
                analysis.text,
                analysis.labels
                    .joinToString(" ")
            )
                .joinToString("\n")
                .lowercase()

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
                    analysis.text,
                labels =
                    analysis.labels
                        .joinToString(", "),
                searchableText =
                    searchableText,
                isAvailable = true,
                lastVerifiedAt = now
            )
        )
    }

    suspend fun verifyAvailability(
        limit: Int =
            DEFAULT_VERIFICATION_BATCH
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

    suspend fun clearIndex() {
        dao.clearAll()
    }

    companion object {
        const val DEFAULT_VERIFICATION_BATCH =
            25
    }
}
