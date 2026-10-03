package com.hadzha3.goldbrain.data

import android.content.Context
import android.net.Uri
import com.hadzha3.goldbrain.ml.LocalImageAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Normalizer

class MemoryRepository(context: Context) {
    private val dao = AppDatabase.get(context).memoryDao()
    private val analyzer = LocalImageAnalyzer(context)

    fun memories(query: String): Flow<List<MemoryEntity>> =
        dao.observeAll().map { items ->
            rank(items, query)
        }

    fun count(): Flow<Int> = dao.count()

    suspend fun exists(uri: Uri): Boolean = dao.exists(uri.toString())

    suspend fun index(
        uri: Uri,
        createdAt: Long = System.currentTimeMillis()
    ) {
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
                createdAt = createdAt,
                category = analysis.category,
                title = analysis.title,
                ocrText = analysis.text,
                labels = analysis.labels.joinToString(", "),
                searchableText = searchable
            )
        )
    }

    private fun rank(
        items: List<MemoryEntity>,
        query: String
    ): List<MemoryEntity> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return items

        val words = normalizedQuery
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 2 }
            .filterNot(stopWords::contains)
            .distinct()

        if (words.isEmpty()) return items

        return items
            .mapNotNull { item ->
                val haystack = normalize(
                    listOf(
                        item.category,
                        item.title,
                        item.ocrText,
                        item.labels,
                        item.searchableText
                    ).joinToString(" ")
                )

                val hits = words.count { word ->
                    haystack.contains(word) ||
                        synonyms[word].orEmpty().any(haystack::contains)
                }

                if (hits == 0) {
                    null
                } else {
                    val exactBonus = if (haystack.contains(normalizedQuery)) 10 else 0
                    val categoryBonus = if (
                        words.any { word ->
                            normalize(item.category).contains(word) ||
                                synonyms[word].orEmpty().any {
                                    normalize(item.category).contains(it)
                                }
                        }
                    ) 3 else 0

                    item to (hits * 5 + exactBonus + categoryBonus)
                }
            }
            .sortedWith(
                compareByDescending<Pair<MemoryEntity, Int>> { it.second }
                    .thenByDescending { it.first.createdAt }
            )
            .map { it.first }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(), Normalizer.Form.NFKC)
            .replace('ё', 'е')
            .replace(Regex("[^\\p{L}\\p{N}@._-]+"), " ")
            .trim()

    private val stopWords = setOf(
        "найди", "найти", "покажи", "фото", "фотку", "фотографию",
        "мне", "мой", "моя", "мою", "который", "которую", "где",
        "из", "от", "на", "в", "и", "the", "a", "an", "find", "show"
    )

    private val synonyms = mapOf(
        "чек" to listOf("receipt", "invoice", "total", "итого"),
        "квитанция" to listOf("receipt", "invoice", "чек"),
        "машина" to listOf("car", "vehicle", "авто"),
        "авто" to listOf("car", "vehicle", "машина"),
        "парковка" to listOf("parking", "car", "vehicle"),
        "книга" to listOf("book", "isbn", "publisher"),
        "билет" to listOf("ticket", "admission", "event"),
        "контакт" to listOf("email", "phone", "tel", "linkedin")
    )
}
