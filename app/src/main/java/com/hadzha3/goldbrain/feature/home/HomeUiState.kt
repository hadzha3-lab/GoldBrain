package com.hadzha3.goldbrain.feature.home

import com.hadzha3.goldbrain.data.local.MemoryEntity

data class HomeUiState(
    val memories: List<MemoryEntity> = emptyList(),
    val memoryCount: Int = 0,
    val query: String = "",
    val isIndexing: Boolean = false,
    val statusText: String = ""
)
