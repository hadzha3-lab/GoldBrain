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
    fun pagedMergeMatchesWholeListRanking() {
        val items =
            (0 until 5_000)
                .map { index ->
                    MemoryEntity(
                        uri =
                            "memory-$index",
                        createdAt =
                            index.toLong(),
                        category =
                            if (
                                index % 11 == 0
                            ) {
                                "Чек"
                            } else {
                                "Память"
                            },
                        title =
                            "Фото $index",
                        ocrText =
                            if (
                                index % 17 == 0
                            ) {
                                "ИТОГО ${index * 10} ₽"
                            } else {
                                ""
                            },
                        labels =
                            when {
                                index == 4_321 ->
                                    "Headphones, Blue"

                                index % 13 == 0 ->
                                    "Headphones"

                                else ->
                                    "Landscape"
                            }
                    )
                }

        val engine =
            MemorySearchEngine()

        val whole =
            engine.search(
                items,
                "синие наушники"
            )

        val paged =
            items.chunked(
                250
            ).fold(
                emptyList<MemoryEntity>()
            ) {
                    best,
                    page ->
                engine.search(
                    best + page,
                    "синие наушники"
                )
            }

        assertEquals(
            whole.map {
                it.uri
            },
            paged.map {
                it.uri
            }
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
