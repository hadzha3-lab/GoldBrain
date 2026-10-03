package com.hadzha3.goldbrain.feature.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.appContainer
import com.hadzha3.goldbrain.background.GalleryIndexScheduler
import com.hadzha3.goldbrain.data.local.MemoryEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    app: Application
) : AndroidViewModel(app) {
    private val container =
        app.appContainer

    private val repository =
        container.memoryRepository

    private val indexStatusRepository =
        container.indexStatusRepository

    private val preferences =
        container.indexingPreferences

    private val query =
        MutableStateFlow("")

    private val localIndexing =
        MutableStateFlow(false)

    private val localStatus =
        MutableStateFlow("")

    private val selectedUri =
        MutableStateFlow<String?>(null)

    private val memories =
        query.flatMapLatest(
            repository::memories
        )

    private val memoryCount =
        repository.count()

    private val backgroundStatus =
        indexStatusRepository.observe()

    private val indexingState =
        combine(
            localIndexing,
            backgroundStatus
        ) { local, background ->
            local ||
                background.isRunning
        }

    private val displayStatus =
        combine(
            localStatus,
            backgroundStatus
        ) { local, background ->
            when {
                local.isNotBlank() ->
                    local

                background.isRunning &&
                    background.totalInRun > 0 ->
                    getApplication<Application>()
                        .getString(
                            R.string.index_background_progress,
                            background.processedInRun,
                            background.totalInRun
                        )

                background.isRunning ->
                    getApplication<Application>()
                        .getString(
                            R.string.index_background_running
                        )

                background.hasError ->
                    getApplication<Application>()
                        .getString(
                            R.string.index_background_error
                        )

                background.indexedInRun > 0 ->
                    getApplication<Application>()
                        .getString(
                            R.string.index_background_done,
                            background.indexedInRun
                        )

                else -> ""
            }
        }

    val uiState = combine(
        memories,
        memoryCount,
        query,
        indexingState,
        displayStatus
    ) {
            items,
            count,
            currentQuery,
            indexing,
            status ->
        HomeUiState(
            memories = items,
            memoryCount = count,
            query = currentQuery,
            isIndexing = indexing,
            statusText = status
        )
    }.stateIn(
        viewModelScope,
        SharingStarted
            .WhileSubscribed(
                5_000
            ),
        HomeUiState()
    )

    val selectedMemory =
        selectedUri
            .flatMapLatest { uri ->
                if (uri == null) {
                    flowOf<MemoryEntity?>(
                        null
                    )
                } else {
                    repository.memory(
                        uri
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted
                    .WhileSubscribed(
                        5_000
                    ),
                null
            )

    init {
        viewModelScope.launch {
            repository
                .verifyAvailability()
        }
    }

    fun onQueryChange(
        value: String
    ) {
        query.value = value
    }

    fun openMemory(
        uri: String
    ) {
        selectedUri.value = uri
    }

    fun closeMemory() {
        selectedUri.value = null
    }

    fun indexSelected(
        uris: List<Uri>
    ) = viewModelScope.launch {
        if (uris.isEmpty()) {
            return@launch
        }

        localIndexing.value =
            true

        var failed = 0

        uris.forEachIndexed {
                index,
                uri ->
            localStatus.value =
                getApplication<Application>()
                    .getString(
                        R.string.index_progress,
                        index + 1,
                        uris.size
                    )

            runCatching {
                repository.index(
                    uri
                )
            }.onFailure {
                failed++
            }
        }

        val finalStatus =
            if (failed == 0) {
                getApplication<Application>()
                    .getString(
                        R.string.index_done
                    )
            } else {
                getApplication<Application>()
                    .getString(
                        R.string.index_done_with_errors,
                        failed
                    )
            }

        localStatus.value =
            finalStatus

        localIndexing.value =
            false

        delay(
            LOCAL_STATUS_DURATION_MS
        )

        if (
            localStatus.value ==
            finalStatus
        ) {
            localStatus.value = ""
        }
    }

    fun startGalleryIndex() {
        localStatus.value = ""

        GalleryIndexScheduler
            .startNow(
                getApplication()
            )
    }

    fun enablePeriodicGalleryIndex() {
        if (
            preferences
                .autoIndexEnabled
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

    private companion object {
        const val LOCAL_STATUS_DURATION_MS =
            2_500L
    }
}
