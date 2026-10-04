package com.hadzha3.goldbrain.data.gallery

import kotlinx.coroutines.CancellationException

enum class GalleryIndexErrorAction {
    CANCEL,
    STOP_FOR_PERMISSION,
    RECORD_PHOTO_FAILURE
}

object GalleryIndexErrorPolicy {
    fun actionFor(
        error: Exception
    ): GalleryIndexErrorAction =
        when (error) {
            is CancellationException ->
                GalleryIndexErrorAction.CANCEL

            is SecurityException ->
                GalleryIndexErrorAction.STOP_FOR_PERMISSION

            else ->
                GalleryIndexErrorAction.RECORD_PHOTO_FAILURE
        }
}
