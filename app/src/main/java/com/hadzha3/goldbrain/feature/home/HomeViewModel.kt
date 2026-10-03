package com.hadzha3.goldbrain.feature.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.appContainer
import com.hadzha3.goldbrain.background.GalleryIndexScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    app: Application
) : AndroidViewModel(app) {
    private val repository =
        app.appContainer.memoryRepository

    private val query = MutableStateFlow("")
    private val isIndexing = MutableStateFlow(false)
    private val statusText = MutableStateFlow("")

    private val memories = query
        .flatMapLatest(repository::memories)

    private val memoryCount =
        repository.count()

    val uiState = combine(
        memories,
        memoryCount,
        query,
        isIndexing,
        statusText
    ) { items, count, currentQuery, indexing, status ->
        HomeUiState(
            memories = items,
            memoryCount = count,
            query = currentQuery,
            isIndexing = indexing,
            statusText = status
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )

    fun onQueryChange(
        value: String
    ) {
        query.value = value
    }

    fun indexSelected(
        uris: List<Uri>
    ) = viewModelScope.launch {
        if (uris.isEmpty()) return@launch

        isIndexing.value = true
        var failed = 0

        uris.forEachIndexed { index, uri ->
            statusText.value = getApplication<Application>()
                .getString(
                    R.string.index_progress,
                    index + 1,
                    uris.size
                )

            runCatching {
                repository.index(uri)
            }.onFailure {
                failed++
            }
        }

        statusText.value =
            if (failed == 0) {
                getApplication<Application>()
                    .getString(R.string.index_done)
            } else {
                getApplication<Application>()
                    .getString(
                        R.string.index_done_with_errors,
                        failed
                    )
            }

        isIndexing.value = false
    }

    fun startGalleryIndex() {
        GalleryIndexScheduler.startNow(
            getApplication()
        )

        statusText.value = getApplication<Application>()
            .getString(R.string.index_gallery_background)
    }

    fun enablePeriodicGalleryIndex() {
        GalleryIndexScheduler.ensurePeriodic(
            getApplication()
        )
    }
}
