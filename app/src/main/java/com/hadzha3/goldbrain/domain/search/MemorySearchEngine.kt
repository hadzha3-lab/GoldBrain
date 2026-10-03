package com.hadzha3.goldbrain.domain.search

import com.hadzha3.goldbrain.data.local.MemoryEntity
import java.text.Normalizer

class MemorySearchEngine {
    fun search(
        items: List<MemoryEntity>,
        query: String
    ): List<MemoryEntity> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return items

        val words = normalizedQuery
            .split(WHITESPACE)
            .asSequence()
            .map(String::trim)
            .filter { it.length >= MIN_TERM_LENGTH }
            .filterNot(STOP_WORDS::contains)
            .distinct()
            .toList()

        if (words.isEmpty()) return items

        return items
            .mapNotNull { item ->
                score(item, normalizedQuery, words)
                    ?.let { score -> item to score }
            }
            .sortedWith(
                compareByDescending<Pair<MemoryEntity, Int>> { it.second }
                    .thenByDescending { it.first.createdAt }
            )
            .map { it.first }
    }

    private fun score(
        item: MemoryEntity,
        normalizedQuery: String,
        words: List<String>
    ): Int? {
        val category = normalize(item.category)
        val haystack = normalize(
            listOf(
                item.category,
                item.title,
                item.ocrText,
                item.labels,
                item.searchableText
            ).joinToString(" ")
        )

        val hits = words.count { term ->
            matches(haystack, term)
        }

        if (hits == 0) return null

        val exactPhraseBonus =
            if (haystack.contains(normalizedQuery)) EXACT_PHRASE_BONUS else 0

        val categoryBonus =
            if (words.any { matches(category, it) }) CATEGORY_BONUS else 0

        return hits * TERM_SCORE + exactPhraseBonus + categoryBonus
    }

    private fun matches(
        haystack: String,
        term: String
    ): Boolean =
        haystack.contains(term) ||
            SYNONYMS[term].orEmpty().any(haystack::contains)

    private fun normalize(
        value: String
    ): String =
        Normalizer.normalize(
            value.lowercase(),
            Normalizer.Form.NFKC
        )
            .replace('ё', 'е')
            .replace(NON_SEARCHABLE, " ")
            .trim()

    private companion object {
        const val MIN_TERM_LENGTH = 2
        const val TERM_SCORE = 5
        const val EXACT_PHRASE_BONUS = 10
        const val CATEGORY_BONUS = 3

        val WHITESPACE = Regex("\\s+")
        val NON_SEARCHABLE = Regex("[^\\p{L}\\p{N}@._-]+")

        val STOP_WORDS = setOf(
            "найди", "найти", "покажи", "фото", "фотку", "фотографию",
            "мне", "мой", "моя", "мою", "который", "которую", "где",
            "из", "от", "на", "в", "и", "the", "a", "an", "find", "show"
        )

        val SYNONYMS = mapOf(
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
}
