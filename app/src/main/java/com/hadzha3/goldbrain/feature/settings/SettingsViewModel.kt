package com.hadzha3.goldbrain.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hadzha3.goldbrain.appContainer
import com.hadzha3.goldbrain.background.GalleryIndexScheduler
import com.hadzha3.goldbrain.core.permissions.GalleryAccessMode
import com.hadzha3.goldbrain.core.permissions.MediaPermissions
import com.hadzha3.goldbrain.data.storage.LocalStorageUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    app: Application
) : AndroidViewModel(app) {
    private val container =
        app.appContainer

    private val repository =
        container.memoryRepository

    private val preferences =
        container.indexingPreferences

    private val indexStatusRepository =
        container.indexStatusRepository

    private val accessMode =
        MutableStateFlow(
            MediaPermissions
                .galleryAccessMode(
                    app
                )
        )

    private val autoIndexEnabled =
        MutableStateFlow(
            preferences
                .autoIndexEnabled
        )

    private val storageUsage =
        MutableStateFlow(
            LocalStorageUsage(
                    indexBytes = 0L,
                    cacheBytes = 0L
                )
        )

    private val coreState =
        combine(
            repository.count(),
            repository.unavailableCount(),
            autoIndexEnabled,
            accessMode,
            indexStatusRepository.observe()
        ) {
                count,
                unavailable,
                auto,
                access,
                status ->
            SettingsUiState(
                memoryCount =
                    count,
                unavailableCount =
                    unavailable,
                autoIndexEnabled =
                    auto,
                galleryAccessMode =
                    access,
                isIndexing =
                    status.isRunning,
                indexedInRun =
                    status.indexedInRun,
                failedInRun =
                    status.failedInRun,
                totalInRun =
                    status.totalInRun,
                progressFraction =
                    status.progressFraction
            )
        }

    val uiState =
        combine(
            coreState,
            storageUsage
        ) {
                state,
                storage ->
            state.copy(
                indexBytes =
                    storage.indexBytes,
                cacheBytes =
                    storage.cacheBytes
            )
        }.stateIn(
            viewModelScope,
            SharingStarted
                .WhileSubscribed(
                    5_000
                ),
            SettingsUiState(
                autoIndexEnabled =
                    preferences
                        .autoIndexEnabled,
                galleryAccessMode =
                    accessMode.value,
                indexBytes =
                    storageUsage
                        .value
                        .indexBytes,
                cacheBytes =
                    storageUsage
                        .value
                        .cacheBytes
            )
        )

    fun refreshStorage() {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storageUsage.value =
                container
                    .localStorageManager
                    .usage()
        }
    }

    fun clearTemporaryCache() {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            container
                .localStorageManager
                .clearTemporaryCache()

            storageUsage.value =
                container
                    .localStorageManager
                    .usage()
        }
    }

    fun refreshPermissions() {
        accessMode.value =
            MediaPermissions
                .galleryAccessMode(
                    getApplication()
                )

        if (
            accessMode.value ==
            GalleryAccessMode.NONE
        ) {
            GalleryIndexScheduler
                .cancelAll(
                    getApplication()
                )

            viewModelScope.launch {
                indexStatusRepository
                    .reset()
            }
        }
    }

    fun onPermissionsChanged() {
        refreshPermissions()

        if (
            accessMode.value !=
            GalleryAccessMode.NONE &&
            autoIndexEnabled.value
        ) {
            GalleryIndexScheduler
                .ensurePeriodic(
                    getApplication()
                )
        }
    }

    fun setAutoIndexEnabled(
        enabled: Boolean
    ) {
        preferences.autoIndexEnabled =
            enabled

        autoIndexEnabled.value =
            enabled

        if (
            enabled &&
            accessMode.value !=
            GalleryAccessMode.NONE
        ) {
            GalleryIndexScheduler
                .ensurePeriodic(
                    getApplication()
                )
        } else {
            GalleryIndexScheduler
                .cancelPeriodic(
                    getApplication()
                )
        }
    }

    fun startIndexing() {
        if (
            accessMode.value ==
            GalleryAccessMode.NONE
        ) {
            return
        }

        GalleryIndexScheduler
            .startNow(
                getApplication()
            )
    }

    fun recheckOriginals() {
        viewModelScope.launch {
            repository
                .verifyAvailability(
                    VERIFY_ALL_LIMIT
                )
        }
    }

    fun clearIndex() {
        viewModelScope.launch {
            GalleryIndexScheduler
                .cancelAll(
                    getApplication()
                )

            preferences.autoIndexEnabled =
                false

            autoIndexEnabled.value =
                false

            container
                .galleryIndexer
                .clearIndex()

            indexStatusRepository
                .reset()

            container
                .persistedMediaPermissionManager
                .releaseAllReadGrants()

            refreshStorage()
        }
    }

    init {
        refreshStorage()
    }

    private companion object {
        const val VERIFY_ALL_LIMIT =
            100_000
    }
}
