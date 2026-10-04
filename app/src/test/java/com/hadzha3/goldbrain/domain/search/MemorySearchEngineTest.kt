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
    fun userNoteParticipatesInSearch() {
        val key =
            memory(
                uri = "spare-key",
                labels = "Key",
                note =
                    "Запасной лежит в верхнем ящике"
            )

        val result =
            searchEngine.search(
                listOf(key),
                "верхний ящик"
            )

        assertEquals(
            "spare-key",
            result.first().uri
        )
    }

    @Test
    fun phoneIntentFindsRawPhoneNumber() {
        val contact =
            memory(
                uri = "contact",
                text =
                    "+7 (999) 123-45-67"
            )

        val result =
            searchEngine.search(
                listOf(contact),
                "найди телефон"
            )

        assertEquals(
            "contact",
            result.first().uri
        )
    }

    @Test
    fun priceIntentCombinesObjectAndReceiptAmount() {
        val headphones =
            memory(
                uri = "headphones-receipt",
                text =
                    "ИТОГО 19 990 ₽",
                labels =
                    "Headphones"
            )

        val result =
            searchEngine.search(
                listOf(headphones),
                "сколько стоили наушники"
            )

        assertEquals(
            "headphones-receipt",
            result.first().uri
        )
    }

    @Test
    fun linkIntentFindsRawWebsite() {
        val page =
            memory(
                uri = "website",
                text =
                    "www.example.com"
            )

        val result =
            searchEngine.search(
                listOf(page),
                "покажи ссылку"
            )

        assertEquals(
            "website",
            result.first().uri
        )
    }

    @Test
    fun downloadedPhotoQueryFiltersOutCameraCopies() {
        val downloaded =
            memory(
                uri = "downloaded",
                labels = "Headphones",
                sourceType = "DOWNLOAD"
            )

        val camera =
            memory(
                uri = "camera",
                labels = "Headphones",
                sourceType = "CAMERA"
            )

        val result =
            searchEngine.search(
                listOf(
                    camera,
                    downloaded
                ),
                "покажи скачанные фото наушников"
            )

        assertEquals(
            listOf(
                "downloaded"
            ),
            result.map {
                it.uri
            }
        )
    }

    @Test
    fun myPhotosIncludeCameraAndGoldBrain() {
        val camera =
            memory(
                uri = "camera",
                labels = "Car",
                sourceType = "CAMERA"
            )

        val goldBrain =
            memory(
                uri = "goldbrain",
                labels = "Car",
                sourceType = "GOLDBRAIN"
            )

        val download =
            memory(
                uri = "download",
                labels = "Car",
                sourceType = "DOWNLOAD"
            )

        val result =
            searchEngine.search(
                listOf(
                    download,
                    goldBrain,
                    camera
                ),
                "найди мои фото машины"
            )

        assertEquals(
            setOf(
                "camera",
                "goldbrain"
            ),
            result.map {
                it.uri
            }.toSet()
        )
    }

    @Test
    fun screenshotSourceCanBeTheWholeQuery() {
        val screenshot =
            memory(
                uri = "screen",
                sourceType =
                    "SCREENSHOT",
                createdAt = 20L
            )

        val camera =
            memory(
                uri = "camera",
                sourceType =
                    "CAMERA",
                createdAt = 30L
            )

        val result =
            searchEngine.search(
                listOf(
                    camera,
                    screenshot
                ),
                "скриншоты"
            )

        assertEquals(
            listOf(
                "screen"
            ),
            result.map {
                it.uri
            }
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
        labels: String = "",
        note: String = "",
        sourceType: String = "UNKNOWN"
    ) = MemoryEntity(
        uri = uri,
        createdAt = createdAt,
        category = category,
        title = title,
        ocrText = text,
        labels = labels,
        userNote = note,
        sourceType = sourceType
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
