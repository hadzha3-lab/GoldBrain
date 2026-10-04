package com.hadzha3.goldbrain.domain.source

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoSourceClassifierTest {
    private val classifier =
        PhotoSourceClassifier(
            "com.hadzha3.goldbrain"
        )

    @Test
    fun goldBrainCaptureIsExact() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    authority =
                        "com.hadzha3.goldbrain.fileprovider",
                    displayName =
                        "GoldBrain_123.jpg"
                )
            )

        assertEquals(
            PhotoSourceType.GOLDBRAIN,
            result.type
        )

        assertEquals(
            100,
            result.confidence
        )
    }

    @Test
    fun downloadFolderBeatsCameraLikeFilename() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    displayName =
                        "IMG_1234.jpg",
                    relativePath =
                        "Download/"
                )
            )

        assertEquals(
            PhotoSourceType.DOWNLOAD,
            result.type
        )
    }

    @Test
    fun cameraFolderIsDetected() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    displayName =
                        "IMG_20261004_120000.jpg",
                    relativePath =
                        "DCIM/Camera/",
                    bucketDisplayName =
                        "Camera"
                )
            )

        assertEquals(
            PhotoSourceType.CAMERA,
            result.type
        )

        assertEquals(
            95,
            result.confidence
        )
    }

    @Test
    fun screenshotsFolderIsDetected() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    displayName =
                        "Screenshot_20261004.png",
                    relativePath =
                        "Pictures/Screenshots/"
                )
            )

        assertEquals(
            PhotoSourceType.SCREENSHOT,
            result.type
        )
    }

    @Test
    fun messengerFolderIsDetected() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    displayName =
                        "IMG-20261004-WA0001.jpg",
                    relativePath =
                        "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/"
                )
            )

        assertEquals(
            PhotoSourceType.MESSENGER,
            result.type
        )
    }

    @Test
    fun weakCameraFilenameStaysLowerConfidence() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    displayName =
                        "IMG_123456.jpg"
                )
            )

        assertEquals(
            PhotoSourceType.CAMERA,
            result.type
        )

        assertEquals(
            62,
            result.confidence
        )
    }

    @Test
    fun unknownMetadataIsNotGuessed() {
        val result =
            classifier.classify(
                PhotoSourceMetadata(
                    displayName =
                        "holiday.jpg"
                )
            )

        assertEquals(
            PhotoSourceType.UNKNOWN,
            result.type
        )

        assertEquals(
            0,
            result.confidence
        )
    }
}
