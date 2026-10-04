package com.hadzha3.goldbrain.data.gallery

import android.net.Uri

data class GalleryMediaItem(
    val uri: Uri,
    val createdAt: Long
)

data class GalleryScanResult(
    val nextOffset: Int,
    val reachedEnd: Boolean
)

interface GalleryMediaSource {
    suspend fun scanImages(
        startOffset: Int,
        pageSize: Int,
        onPage:
            suspend (
                List<GalleryMediaItem>
            ) -> Boolean
    ): GalleryScanResult
}
