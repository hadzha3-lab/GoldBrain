package com.hadzha3.goldbrain.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hadzha3.goldbrain.data.MemoryEntity
import com.hadzha3.goldbrain.data.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    app: Application
) : AndroidViewModel(app) {
    private val repository = MemoryRepository(app)

    val query = MutableStateFlow("")

    val items: StateFlow<List<MemoryEntity>> = query
        .flatMapLatest(repository::memories)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val count = repository.count()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val indexing = MutableStateFlow(false)
    val progress = MutableStateFlow("")

    fun index(uris: List<Uri>) = viewModelScope.launch {
        if (uris.isEmpty()) return@launch

        indexing.value = true
        var failed = 0

        uris.forEachIndexed { index, uri ->
            progress.value = "Анализ ${index + 1} из ${uris.size}"
            if (runCatching { repository.index(uri) }.isFailure) {
                failed++
            }
        }

        progress.value = if (failed == 0) {
            "Готово"
        } else {
            "Готово · ошибок: $failed"
        }
        indexing.value = false
    }
}
