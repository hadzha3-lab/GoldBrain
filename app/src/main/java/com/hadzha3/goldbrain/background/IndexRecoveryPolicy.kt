package com.hadzha3.goldbrain.background

enum class IndexRecoveryAction {
    NONE,
    RESTART,
    RESET
}

object IndexRecoveryPolicy {
    fun decide(
        statusRunning: Boolean,
        hasActiveWork: Boolean,
        hasGalleryAccess: Boolean
    ): IndexRecoveryAction =
        when {
            !statusRunning ->
                IndexRecoveryAction.NONE

            hasActiveWork ->
                IndexRecoveryAction.NONE

            hasGalleryAccess ->
                IndexRecoveryAction.RESTART

            else ->
                IndexRecoveryAction.RESET
        }
}
