package com.hadzha3.goldbrain.background

import android.content.Context
import com.hadzha3.goldbrain.core.permissions.MediaPermissions
import com.hadzha3.goldbrain.data.preferences.IndexingPreferences
import com.hadzha3.goldbrain.data.repository.IndexStatusRepository

class IndexRecoveryCoordinator(
    private val context: Context,
    private val statusRepository:
        IndexStatusRepository,
    private val preferences:
        IndexingPreferences
) {
    suspend fun recover() {
        val status =
            statusRepository.current()

        val activeWork =
            GalleryIndexScheduler
                .hasActiveIndexWork(
                    context
                )

        when (
            IndexRecoveryPolicy.decide(
                statusRunning =
                    status.isRunning,
                hasActiveWork =
                    activeWork,
                hasGalleryAccess =
                    MediaPermissions
                        .hasGalleryAccess(
                            context
                        )
            )
        ) {
            IndexRecoveryAction.NONE ->
                Unit

            IndexRecoveryAction.RESTART ->
                GalleryIndexScheduler
                    .startNow(
                        context
                    )

            IndexRecoveryAction.RESET ->
                statusRepository.reset()
        }

        if (
            preferences.autoIndexEnabled &&
            MediaPermissions
                .hasGalleryAccess(
                    context
                )
        ) {
            GalleryIndexScheduler
                .ensurePeriodic(
                    context
                )
        }
    }
}
