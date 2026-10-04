package com.hadzha3.goldbrain.data.gallery

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryIndexErrorPolicyTest {
    @Test
    fun cancellationIsNeverRecordedAsPhotoFailure() {
        assertEquals(
            GalleryIndexErrorAction.CANCEL,
            GalleryIndexErrorPolicy
                .actionFor(
                    CancellationException()
                )
        )
    }

    @Test
    fun revokedPermissionStopsGalleryRun() {
        assertEquals(
            GalleryIndexErrorAction
                .STOP_FOR_PERMISSION,
            GalleryIndexErrorPolicy
                .actionFor(
                    SecurityException(
                        "permission revoked"
                    )
                )
        )
    }

    @Test
    fun ordinaryImageFailureIsRecorded() {
        assertEquals(
            GalleryIndexErrorAction
                .RECORD_PHOTO_FAILURE,
            GalleryIndexErrorPolicy
                .actionFor(
                    IllegalArgumentException(
                        "bad image"
                    )
                )
        )
    }
}
