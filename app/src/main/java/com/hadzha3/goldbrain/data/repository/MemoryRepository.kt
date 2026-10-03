package com.hadzha3.goldbrain.data.repository

import android.net.Uri
import com.hadzha3.goldbrain.data.analysis.ImageAnalyzer
import com.hadzha3.goldbrain.data.local.MemoryDao
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.domain.search.MemorySearchEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepository(
    private val dao: MemoryDao,
    private val imageAnalyzer: ImageAnalyzer,
    private val searchEngine: MemorySearchEngine
) {
    fun memories(
        query: String
    ): Flow<List<MemoryEntity>> =
        dao.observeAll().map { items ->
            searchEngine.search(items, query)
        }

    fun count(): Flow<Int> =
        dao.count()

    suspend fun indexedUris(): Set<String> =
        dao.allUris().toHashSet()

    suspend fun index(
        uri: Uri,
        createdAt: Long = System.currentTimeMillis()
    ) {
        val analysis = imageAnalyzer.analyze(uri)
        val searchableText = listOf(
            analysis.title,
            analysis.category,
            analysis.text,
            analysis.labels.joinToString(" ")
        )
            .joinToString("\n")
            .lowercase()

        dao.upsert(
            MemoryEntity(
                uri = uri.toString(),
                createdAt = createdAt,
                category = analysis.category,
                title = analysis.title,
                ocrText = analysis.text,
                labels = analysis.labels.joinToString(", "),
                searchableText = searchableText
            )
        )
    }
}
