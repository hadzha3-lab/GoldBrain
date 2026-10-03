package com.hadzha3.goldbrain.data.gallery

import android.net.Uri

data class GalleryMediaItem(
    val uri: Uri,
    val createdAt: Long
)

interface GalleryMediaSource {
    fun unindexedImages(
        indexedUris: Set<String>,
        limit: Int
    ): List<GalleryMediaItem>
}
