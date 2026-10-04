package com.hadzha3.goldbrain.feature.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.appContainer
import com.hadzha3.goldbrain.background.GalleryIndexScheduler
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.feature.detail.MemoryDetailActionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(FlowPreview::class)
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

    private val selectedIndexMutex =
        Mutex()

    private val _detailActionState =
        MutableStateFlow(
            MemoryDetailActionState()
        )

    val detailActionState =
        _detailActionState

    private val memories =
        query
            .debounce(
                SEARCH_DEBOUNCE_MS
            )
            .distinctUntilChanged()
            .flatMapLatest(
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

    fun reanalyzeMemory(
        memory: MemoryEntity
    ) {
        if (
            _detailActionState
                .value
                .isBusy
        ) {
            return
        }

        viewModelScope.launch {
            _detailActionState.value =
                MemoryDetailActionState(
                    isBusy = true
                )

            try {
                repository.index(
                    uri =
                        Uri.parse(
                            memory.uri
                        ),
                    createdAt =
                        memory.createdAt
                )

                container
                    .indexFailureRepository
                    .clear(
                        memory.uri
                    )

                showDetailMessage(
                    getApplication<Application>()
                        .getString(
                            R.string.detail_reanalyze_done
                        )
                )
            } catch (
                cancellation:
                    CancellationException
            ) {
                throw cancellation
            } catch (_: Exception) {
                showDetailMessage(
                    getApplication<Application>()
                        .getString(
                            R.string.detail_reanalyze_failed
                        )
                )
            }
        }
    }

    fun removeMemory(
        uri: String
    ) {
        if (
            _detailActionState
                .value
                .isBusy
        ) {
            return
        }

        viewModelScope.launch {
            _detailActionState.value =
                MemoryDetailActionState(
                    isBusy = true
                )

            try {
                container
                    .galleryIndexer
                    .ignoreAndRemove(
                        uri
                    )

                repository
                    .notifyIndexChanged()

                container
                    .persistedMediaPermissionManager
                    .releaseReadGrant(
                        Uri.parse(
                            uri
                        )
                    )

                selectedUri.value =
                    null

                _detailActionState.value =
                    MemoryDetailActionState()
            } catch (
                cancellation:
                    CancellationException
            ) {
                throw cancellation
            } catch (_: Exception) {
                showDetailMessage(
                    getApplication<Application>()
                        .getString(
                            R.string.detail_remove_failed
                        )
                )
            }
        }
    }

    fun updateMemoryNote(
        uri: String,
        note: String
    ) {
        viewModelScope.launch {
            repository
                .updateUserNote(
                    uri = uri,
                    note = note
                )
        }
    }

    fun indexSelected(
        uris: List<Uri>
    ) = viewModelScope.launch {
        if (uris.isEmpty()) {
            return@launch
        }

        selectedIndexMutex
            .withLock {
                localIndexing.value =
                    true

                var failed = 0

                try {
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

                        try {
                            container
                                .memoryIndexMaintenanceRepository
                                .allow(
                                    uri.toString()
                                )

                            repository.index(
                                uri
                            )
                        } catch (
                            cancellation:
                                CancellationException
                        ) {
                            throw cancellation
                        } catch (_: Exception) {
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

                    delay(
                        LOCAL_STATUS_DURATION_MS
                    )

                    if (
                        localStatus.value ==
                        finalStatus
                    ) {
                        localStatus.value = ""
                    }
                } catch (
                    cancellation:
                        CancellationException
                ) {
                    localStatus.value =
                        ""

                    throw cancellation
                } finally {
                    localIndexing.value =
                        false
                }
            }
    }

    private suspend fun showDetailMessage(
        message: String
    ) {
        _detailActionState.value =
            MemoryDetailActionState(
                isBusy = false,
                message = message
            )

        delay(
            DETAIL_STATUS_DURATION_MS
        )

        if (
            _detailActionState
                .value
                .message ==
            message
        ) {
            _detailActionState.value =
                MemoryDetailActionState()
        }
    }

    fun showGalleryPermissionDenied() {
        viewModelScope.launch {
            val message =
                getApplication<Application>()
                    .getString(
                        R.string.gallery_permission_denied
                    )

            localStatus.value =
                message

            delay(
                LOCAL_STATUS_DURATION_MS
            )

            if (
                localStatus.value ==
                message
            ) {
                localStatus.value =
                    ""
            }
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

        const val SEARCH_DEBOUNCE_MS =
            140L

        const val DETAIL_STATUS_DURATION_MS =
            2_500L
    }
}
