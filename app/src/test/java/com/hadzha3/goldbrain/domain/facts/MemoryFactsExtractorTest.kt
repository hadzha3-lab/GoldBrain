package com.hadzha3.goldbrain.domain.facts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryFactsExtractorTest {
    @Test
    fun extractsReceiptAmount() {
        val facts =
            MemoryFactsExtractor.extract(
                "ИТОГО 19 990 ₽"
            )

        assertTrue(
            facts.any {
                it.type ==
                    MemoryFactType.MONEY &&
                    it.value.contains(
                        "19 990"
                    )
            }
        )
    }

    @Test
    fun extractsContactDetails() {
        val facts =
            MemoryFactsExtractor.extract(
                """
                Иван
                +7 (999) 123-45-67
                ivan@example.com
                www.example.com
                """.trimIndent()
            )

        assertTrue(
            facts.any {
                it.type ==
                    MemoryFactType.PHONE
            }
        )

        assertTrue(
            facts.any {
                it.type ==
                    MemoryFactType.EMAIL
            }
        )

        assertTrue(
            facts.any {
                it.type ==
                    MemoryFactType.URL
            }
        )
    }

    @Test
    fun extractsRussianDateAndIsbn() {
        val facts =
            MemoryFactsExtractor.extract(
                """
                15 июля 2026
                ISBN 978-5-17-123456-7
                """.trimIndent()
            )

        assertTrue(
            facts.any {
                it.type ==
                    MemoryFactType.DATE
            }
        )

        assertTrue(
            facts.any {
                it.type ==
                    MemoryFactType.ISBN
            }
        )
    }

    @Test
    fun searchableTextAddsHumanIntentAliases() {
        val text =
            MemoryFactsExtractor
                .searchableText(
                    listOf(
                        MemoryFact(
                            type =
                                MemoryFactType.PHONE,
                            value =
                                "+7 999 123 45 67"
                        )
                    )
                )

        assertTrue(
            text.contains(
                "телефон"
            )
        )

        assertTrue(
            text.contains(
                "+7 999 123 45 67"
            )
        )
    }

    @Test
    fun duplicateFactsAreRemoved() {
        val facts =
            MemoryFactsExtractor.extract(
                "test@example.com test@example.com"
            )

        assertEquals(
            1,
            facts.count {
                it.type ==
                    MemoryFactType.EMAIL
            }
        )
    }
}
