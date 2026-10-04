package com.hadzha3.goldbrain.data.gallery

import android.net.Uri
import com.hadzha3.goldbrain.domain.source.PhotoSourceClassification
import com.hadzha3.goldbrain.domain.source.PhotoSourceType

data class GalleryMediaItem(
    val uri: Uri,
    val createdAt: Long,
    val source:
        PhotoSourceClassification =
        PhotoSourceClassification(
            type =
                PhotoSourceType.UNKNOWN,
            confidence = 0
        )
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
