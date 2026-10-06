package com.hadzha3.goldbrain.background

import org.junit.Assert.assertEquals
import org.junit.Test

class IndexRecoveryPolicyMatrixTest {
    @Test
    fun everyRecoveryStateHasDeterministicSafeAction() {
        val cases =
            listOf(
                Case(false, false, false, IndexRecoveryAction.NONE),
                Case(false, false, true, IndexRecoveryAction.NONE),
                Case(false, true, false, IndexRecoveryAction.NONE),
                Case(false, true, true, IndexRecoveryAction.NONE),
                Case(true, false, false, IndexRecoveryAction.RESET),
                Case(true, false, true, IndexRecoveryAction.RESTART),
                Case(true, true, false, IndexRecoveryAction.RESET),
                Case(true, true, true, IndexRecoveryAction.NONE)
            )

        cases.forEach { case ->
            assertEquals(
                case.expected,
                IndexRecoveryPolicy.decide(
                    statusRunning = case.statusRunning,
                    hasActiveWork = case.hasActiveWork,
                    hasGalleryAccess = case.hasGalleryAccess
                )
            )
        }
    }

    private data class Case(
        val statusRunning: Boolean,
        val hasActiveWork: Boolean,
        val hasGalleryAccess: Boolean,
        val expected: IndexRecoveryAction
    )
}
