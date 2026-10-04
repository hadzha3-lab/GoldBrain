package com.hadzha3.goldbrain.background

import org.junit.Assert.assertEquals
import org.junit.Test

class IndexingLoadPolicyTest {
    @Test
    fun chargingUsesFullBatch() {
        assertEquals(
            IndexingLoadPolicy(
                batchSize = 30,
                continuationDelaySeconds = 2
            ),
            IndexingLoadPolicySelector
                .select(
                    isCharging = true,
                    isPowerSaveMode = false
                )
        )
    }

    @Test
    fun batteryUsesModerateBatch() {
        assertEquals(
            IndexingLoadPolicy(
                batchSize = 15,
                continuationDelaySeconds = 6
            ),
            IndexingLoadPolicySelector
                .select(
                    isCharging = false,
                    isPowerSaveMode = false
                )
        )
    }

    @Test
    fun powerSaveOverridesCharging() {
        assertEquals(
            IndexingLoadPolicy(
                batchSize = 5,
                continuationDelaySeconds = 20
            ),
            IndexingLoadPolicySelector
                .select(
                    isCharging = true,
                    isPowerSaveMode = true
                )
        )
    }
}
