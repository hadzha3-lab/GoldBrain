package com.hadzha3.goldbrain.domain.search

import com.hadzha3.goldbrain.data.local.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

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

    @Test
    fun yesterdayFiltersOutOtherDays() {
        val now =
            date(
                year = 2026,
                month = Calendar.OCTOBER,
                day = 3,
                hour = 12
            )

        val engine =
            MemorySearchEngine {
                now
            }

        val yesterday =
            memory(
                uri = "yesterday",
                createdAt = date(
                    year = 2026,
                    month = Calendar.OCTOBER,
                    day = 2,
                    hour = 19
                ),
                category = "Парковка",
                labels = "Parking, Car"
            )

        val today =
            memory(
                uri = "today",
                createdAt = date(
                    year = 2026,
                    month = Calendar.OCTOBER,
                    day = 3,
                    hour = 8
                ),
                category = "Парковка",
                labels = "Parking, Car"
            )

        val result = engine.search(
            listOf(today, yesterday),
            "где я парковался вчера"
        )

        assertEquals(
            listOf("yesterday"),
            result.map { it.uri }
        )
    }

    @Test
    fun summerQueryFindsMostRecentSummer() {
        val now =
            date(
                year = 2026,
                month = Calendar.OCTOBER,
                day = 3
            )

        val engine =
            MemorySearchEngine {
                now
            }

        val summerBook =
            memory(
                uri = "summer-book",
                createdAt = date(
                    year = 2026,
                    month = Calendar.JULY,
                    day = 15
                ),
                category = "Книга",
                labels = "Book"
            )

        val springBook =
            memory(
                uri = "spring-book",
                createdAt = date(
                    year = 2026,
                    month = Calendar.APRIL,
                    day = 15
                ),
                category = "Книга",
                labels = "Book"
            )

        val result = engine.search(
            listOf(springBook, summerBook),
            "как называлась книга которую я фотографировал летом"
        )

        assertEquals(
            listOf("summer-book"),
            result.map { it.uri }
        )
    }

    @Test
    fun redCarQueryMatchesEnglishVisualLabels() {
        val car =
            memory(
                uri = "red-car",
                labels = "Car, Red"
            )

        val other =
            memory(
                uri = "blue-book",
                labels = "Book, Blue"
            )

        val result =
            searchEngine.search(
                listOf(
                    other,
                    car
                ),
                "красная машина"
            )

        assertEquals(
            listOf("red-car"),
            result.map {
                it.uri
            }
        )
    }

    @Test
    fun blueHeadphonesQueryHandlesRussianCases() {
        val headphones =
            memory(
                uri = "headphones",
                labels =
                    "Headphones, Blue"
            )

        val result =
            searchEngine.search(
                listOf(
                    headphones
                ),
                "найди синие наушники"
            )

        assertEquals(
            "headphones",
            result.first().uri
        )
    }

    @Test
    fun whiteBookQueryMatchesInflectedRussianWords() {
        val book =
            memory(
                uri = "white-book",
                labels = "Book, White"
            )

        val result =
            searchEngine.search(
                listOf(book),
                "покажи белую книгу"
            )

        assertEquals(
            "white-book",
            result.first().uri
        )
    }

    @Test
    fun russianKeyQueryMatchesEnglishLabel() {
        val key =
            memory(
                uri = "key",
                labels = "Key"
            )

        val result =
            searchEngine.search(
                listOf(key),
                "где запасной ключ"
            )

        assertEquals(
            "key",
            result.first().uri
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

    private fun date(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12
    ): Long =
        Calendar.getInstance()
            .apply {
                clear()
                set(
                    year,
                    month,
                    day,
                    hour,
                    0,
                    0
                )
            }
            .timeInMillis
}
