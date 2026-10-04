package com.hadzha3.goldbrain.data.repository

import com.hadzha3.goldbrain.data.local.IndexLookupDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class IndexLookupRepositoryTest {
    @Test
    fun emptyBatchDoesNotHitDatabase() =
        runBlocking {
            var called =
                false

            val repository =
                IndexLookupRepository(
                    dao =
                        object :
                            IndexLookupDao {
                            override suspend fun excludedUris(
                                uris: List<String>,
                                now: Long
                            ): List<String> {
                                called = true
                                return emptyList()
                            }
                        },
                    nowProvider = {
                        123L
                    }
                )

            assertEquals(
                emptySet<String>(),
                repository
                    .excludedAmong(
                        emptyList()
                    )
            )

            assertFalse(
                called
            )
        }

    @Test
    fun batchLookupReturnsOnlyExcludedUris() =
        runBlocking {
            val repository =
                IndexLookupRepository(
                    dao =
                        object :
                            IndexLookupDao {
                            override suspend fun excludedUris(
                                uris: List<String>,
                                now: Long
                            ): List<String> {
                                assertEquals(
                                    listOf(
                                        "a",
                                        "b",
                                        "c"
                                    ),
                                    uris
                                )

                                assertEquals(
                                    456L,
                                    now
                                )

                                return listOf(
                                    "a",
                                    "c"
                                )
                            }
                        },
                    nowProvider = {
                        456L
                    }
                )

            assertEquals(
                setOf(
                    "a",
                    "c"
                ),
                repository
                    .excludedAmong(
                        listOf(
                            "a",
                            "b",
                            "c"
                        )
                    )
            )
        }
}
