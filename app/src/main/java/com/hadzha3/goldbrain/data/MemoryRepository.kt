package com.hadzha3.goldbrain.data

import android.content.Context
import android.net.Uri
import com.hadzha3.goldbrain.ml.LocalImageAnalyzer
import kotlinx.coroutines.flow.Flow

class MemoryRepository(context: Context) {
    private val dao = AppDatabase.get(context).memoryDao()
    private val analyzer = LocalImageAnalyzer(context)

    fun memories(query: String): Flow<List<MemoryEntity>> {
        val normalized = query.trim().lowercase()
        return if (normalized.isBlank()) {
            dao.observeAll()
        } else {
            dao.search(normalized)
        }
    }

    fun count(): Flow<Int> = dao.count()

    suspend fun index(uri: Uri) {
        val analysis = analyzer.analyze(uri)
        val searchable = listOf(
            analysis.title,
            analysis.category,
            analysis.text,
            analysis.labels.joinToString(" ")
        ).joinToString("\n").lowercase()

        dao.upsert(
            MemoryEntity(
                uri = uri.toString(),
                category = analysis.category,
                title = analysis.title,
                ocrText = analysis.text,
                labels = analysis.labels.joinToString(", "),
                searchableText = searchable
            )
        )
    }
}
