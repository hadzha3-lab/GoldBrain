package com.hadzha3.goldbrain.domain.search

import com.hadzha3.goldbrain.data.local.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemorySearchEngineTest {
    private val searchEngine =
        MemorySearchEngine()

    @Test
    fun receiptQueryRanksMatchingReceiptFirst() {
        val receipt = memory(
            uri = "receipt",
            category = "Чек",
            text = "Наушники Sony ИТОГО 19990"
        )

        val book = memory(
            uri = "book",
            category = "Книга",
            text = "ISBN Kotlin"
        )

        val result = searchEngine.search(
            listOf(book, receipt),
            "найди чек от наушников"
        )

        assertEquals(
            "receipt",
            result.first().uri
        )
    }

    @Test
    fun synonymMatchesEnglishImageLabel() {
        val car = memory(
            uri = "car",
            category = "Память",
            labels = "Car, Vehicle"
        )

        val result = searchEngine.search(
            listOf(car),
            "машина"
        )

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun blankQueryKeepsOriginalOrder() {
        val first = memory(
            uri = "first",
            createdAt = 2L
        )

        val second = memory(
            uri = "second",
            createdAt = 1L
        )

        assertEquals(
            listOf(first, second),
            searchEngine.search(
                listOf(first, second),
                "   "
            )
        )
    }

    private fun memory(
        uri: String,
        createdAt: Long = 1L,
        category: String = "Память",
        title: String = "",
        text: String = "",
        labels: String = ""
    ) = MemoryEntity(
        uri = uri,
        createdAt = createdAt,
        category = category,
        title = title,
        ocrText = text,
        labels = labels,
        searchableText = listOf(
            category,
            title,
            text,
            labels
        ).joinToString(" ").lowercase()
    )
}
