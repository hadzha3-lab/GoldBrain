package com.hadzha3.goldbrain.domain.index

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndexTextCompactorTest {
    @Test
    fun shortOcrIsNormalizedWithoutGrowing() {
        val compact =
            IndexTextCompactor
                .compactOcr(
                    "  Магазин   Gold  \n\n ИТОГО   1000 ₽ "
                )

        assertEquals(
            "Магазин Gold\nИТОГО 1000 ₽",
            compact
        )
    }

    @Test
    fun longOcrKeepsBeginningAndEnd() {
        val beginning =
            "НАЧАЛО"

        val ending =
            "ИТОГО 19 990 ₽"

        val source =
            beginning +
                " x".repeat(
                    5_000
                ) +
                ending

        val compact =
            IndexTextCompactor
                .compactOcr(
                    source
                )

        assertTrue(
            compact.length <=
                IndexTextCompactor
                    .MAX_OCR_CHARS
        )

        assertTrue(
            compact.startsWith(
                beginning
            )
        )

        assertTrue(
            compact.endsWith(
                ending
            )
        )
    }

    @Test
    fun labelsAreDeduplicatedAndBounded() {
        val compact =
            IndexTextCompactor
                .compactLabels(
                    listOf(
                        "Car",
                        "car",
                        "Vehicle",
                        "Parking",
                        "Blue",
                        "Road",
                        "Outdoor",
                        "Transport",
                        "Extra",
                        "Ignored"
                    )
                )

        val labels =
            compact.split(
                ", "
            )

        assertEquals(
            8,
            labels.size
        )

        assertEquals(
            1,
            labels.count {
                it.equals(
                    "car",
                    ignoreCase = true
                )
            }
        )
    }
}
