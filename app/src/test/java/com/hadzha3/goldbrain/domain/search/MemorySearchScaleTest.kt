package com.hadzha3.goldbrain.domain.search

import com.hadzha3.goldbrain.data.local.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MemorySearchScaleTest {
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
                            },
                        searchableText =
                            if (
                                index ==
                                targetIndex
                            ) {
                                "headphones blue"
                            } else {
                                "landscape"
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
