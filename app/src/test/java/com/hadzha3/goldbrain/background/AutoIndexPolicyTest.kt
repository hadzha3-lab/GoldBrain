package com.hadzha3.goldbrain.background

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoIndexPolicyTest {
    @Test
    fun schedulesOnlyWhenEnabledAndPermissionExists() {
        assertTrue(
            AutoIndexPolicy.shouldSchedule(
                enabled = true,
                hasGalleryAccess = true
            )
        )

        assertFalse(
            AutoIndexPolicy.shouldSchedule(
                enabled = true,
                hasGalleryAccess = false
            )
        )

        assertFalse(
            AutoIndexPolicy.shouldSchedule(
                enabled = false,
                hasGalleryAccess = true
            )
        )

        assertFalse(
            AutoIndexPolicy.shouldSchedule(
                enabled = false,
                hasGalleryAccess = false
            )
        )
    }
}
