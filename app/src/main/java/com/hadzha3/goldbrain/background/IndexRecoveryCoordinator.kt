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

        val hasGalleryAccess =
            MediaPermissions
                .hasGalleryAccess(
                    context
                )

        if (!hasGalleryAccess) {
            GalleryIndexScheduler
                .cancelAll(
                    context
                )

            if (status.isRunning) {
                statusRepository.reset()
            }

            return
        }

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
                    true
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
            preferences.autoIndexEnabled
        ) {
            GalleryIndexScheduler
                .ensurePeriodic(
                    context
                )
        } else {
            GalleryIndexScheduler
                .cancelPeriodic(
                    context
                )
        }
    }
}
