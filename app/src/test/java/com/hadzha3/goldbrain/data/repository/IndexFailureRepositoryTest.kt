package com.hadzha3.goldbrain.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class IndexFailureRepositoryTest {
    @Test
    fun retryBackoffGrowsAcrossFailures() {
        assertEquals(
            60L * 60L * 1000L,
            IndexFailureRepository
                .retryDelayMs(1)
        )

        assertEquals(
            6L * 60L * 60L * 1000L,
            IndexFailureRepository
                .retryDelayMs(2)
        )

        assertEquals(
            24L * 60L * 60L * 1000L,
            IndexFailureRepository
                .retryDelayMs(3)
        )

        assertEquals(
            72L * 60L * 60L * 1000L,
            IndexFailureRepository
                .retryDelayMs(8)
        )
    }
}
