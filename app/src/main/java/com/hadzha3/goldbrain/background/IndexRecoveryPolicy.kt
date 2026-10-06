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
            statusRunning &&
                !hasGalleryAccess ->
                IndexRecoveryAction.RESET

            !statusRunning ->
                IndexRecoveryAction.NONE

            hasActiveWork ->
                IndexRecoveryAction.NONE

            else ->
                IndexRecoveryAction.RESTART
        }
}
