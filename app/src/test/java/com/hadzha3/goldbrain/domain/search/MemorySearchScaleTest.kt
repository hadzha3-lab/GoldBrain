package com.hadzha3.goldbrain.domain.search

import com.hadzha3.goldbrain.data.local.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MemorySearchScaleTest {
    @Test
    fun resultListIsBoundedForHugeMatches() {
        val items =
            (0 until 2_000)
                .map { index ->
                    MemoryEntity(
                        uri =
                            "receipt-$index",
                        createdAt =
                            index.toLong(),
                        category =
                            "Чек",
                        title =
                            "Чек $index",
                        ocrText =
                            "ИТОГО 1000",
                        labels =
                            "Receipt"
                    )
                }

        val result =
            MemorySearchEngine()
                .search(
                    items,
                    "чек"
                )

        assertEquals(
            200,
            result.size
        )

        assertEquals(
            "receipt-1999",
            result.first().uri
        )
    }

    @Test
    fun findsTargetAmongThousandsOfMemories() {
        val targetIndex =
            3_777

        val items =
            (0 until 5_000)
                .map { index ->
                    MemoryEntity(
                        uri =
                            "memory-$index",
                        createdAt =
                            index.toLong(),
                        category =
                            "Память",
                        title =
                            "Фото $index",
                        ocrText =
                            if (
                                index ==
                                targetIndex
                            ) {
                                "ИТОГО 19 990 ₽"
                            } else {
                                ""
                            },
                        labels =
                            if (
                                index ==
                                targetIndex
                            ) {
                                "Headphones, Blue"
                            } else {
                                "Landscape"
                            }
                    )
                }

        val result =
            MemorySearchEngine()
                .search(
                    items,
                    "сколько стоили синие наушники"
                )

        assertEquals(
            "memory-$targetIndex",
            result.first().uri
        )
    }
}
