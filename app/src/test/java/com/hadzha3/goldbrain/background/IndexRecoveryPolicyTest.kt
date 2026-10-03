package com.hadzha3.goldbrain.background

import org.junit.Assert.assertEquals
import org.junit.Test

class IndexRecoveryPolicyTest {
    @Test
    fun idleStatusNeedsNoRecovery() {
        assertEquals(
            IndexRecoveryAction.NONE,
            IndexRecoveryPolicy.decide(
                statusRunning = false,
                hasActiveWork = false,
                hasGalleryAccess = true
            )
        )
    }

    @Test
    fun activeWorkIsNeverDuplicated() {
        assertEquals(
            IndexRecoveryAction.NONE,
            IndexRecoveryPolicy.decide(
                statusRunning = true,
                hasActiveWork = true,
                hasGalleryAccess = true
            )
        )
    }

    @Test
    fun interruptedRunRestartsWhenGalleryIsAvailable() {
        assertEquals(
            IndexRecoveryAction.RESTART,
            IndexRecoveryPolicy.decide(
                statusRunning = true,
                hasActiveWork = false,
                hasGalleryAccess = true
            )
        )
    }

    @Test
    fun interruptedRunIsResetWhenPermissionWasRevoked() {
        assertEquals(
            IndexRecoveryAction.RESET,
            IndexRecoveryPolicy.decide(
                statusRunning = true,
                hasActiveWork = false,
                hasGalleryAccess = false
            )
        )
    }
}
