package com.hadzha3.goldbrain.background

import com.hadzha3.goldbrain.data.gallery.GalleryIndexErrorAction
import com.hadzha3.goldbrain.data.gallery.GalleryIndexErrorPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BetaScenarioPolicyTest {
    @Test
    fun cleanInstallWithoutGalleryAccessStaysIdle() {
        assertFalse(
            AutoIndexPolicy.shouldSchedule(
                enabled = true,
                hasGalleryAccess = false
            )
        )

        assertEquals(
            IndexRecoveryAction.NONE,
            IndexRecoveryPolicy.decide(
                statusRunning = false,
                hasActiveWork = false,
                hasGalleryAccess = false
            )
        )
    }

    @Test
    fun grantingGalleryAccessAllowsAutomaticIndexing() {
        assertTrue(
            AutoIndexPolicy.shouldSchedule(
                enabled = true,
                hasGalleryAccess = true
            )
        )
    }

    @Test
    fun processDeathRestartsInterruptedRunWithoutDuplicateWork() {
        assertEquals(
            IndexRecoveryAction.RESTART,
            IndexRecoveryPolicy.decide(
                statusRunning = true,
                hasActiveWork = false,
                hasGalleryAccess = true
            )
        )

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
    fun revokedPermissionResetsInterruptedStateAndIsNotPhotoFailure() {
        assertEquals(
            IndexRecoveryAction.RESET,
            IndexRecoveryPolicy.decide(
                statusRunning = true,
                hasActiveWork = false,
                hasGalleryAccess = false
            )
        )

        assertEquals(
            GalleryIndexErrorAction.STOP_FOR_PERMISSION,
            GalleryIndexErrorPolicy.actionFor(
                SecurityException(
                    "gallery permission revoked"
                )
            )
        )
    }
}
