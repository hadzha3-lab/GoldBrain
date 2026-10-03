package com.hadzha3.goldbrain.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class IndexStatusTest {
    @Test
    fun progressIncludesDeferredFailures() {
        val status =
            IndexStatus(
                isRunning = true,
                hasError = false,
                indexedInRun = 25,
                failedInRun = 5,
                totalInRun = 100,
                startedAt = 1L,
                lastUpdatedAt = 2L
            )

        assertEquals(
            0.30f,
            status.progressFraction,
            0.0001f
        )
    }

    @Test
    fun progressIsClampedToOne() {
        val status =
            IndexStatus(
                isRunning = false,
                hasError = false,
                indexedInRun = 120,
                failedInRun = 0,
                totalInRun = 100,
                startedAt = 1L,
                lastUpdatedAt = 2L
            )

        assertEquals(
            1f,
            status.progressFraction,
            0.0001f
        )
    }
}
