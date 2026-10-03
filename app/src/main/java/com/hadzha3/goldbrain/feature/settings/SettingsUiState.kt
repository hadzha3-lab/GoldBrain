package com.hadzha3.goldbrain.feature.settings

import com.hadzha3.goldbrain.core.permissions.GalleryAccessMode

data class SettingsUiState(
    val memoryCount: Int = 0,
    val unavailableCount: Int = 0,
    val autoIndexEnabled: Boolean = true,
    val galleryAccessMode: GalleryAccessMode =
        GalleryAccessMode.NONE,
    val isIndexing: Boolean = false,
    val indexedInRun: Int = 0,
    val failedInRun: Int = 0,
    val totalInRun: Int = 0,
    val progressFraction: Float = 0f,
    val indexBytes: Long = 0L,
    val cacheBytes: Long = 0L
)
