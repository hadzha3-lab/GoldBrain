package com.hadzha3.goldbrain.domain.source

class PhotoSourceClassifier(
    private val appPackageName: String
) {
    fun classify(
        metadata: PhotoSourceMetadata
    ): PhotoSourceClassification {
        val authority =
            metadata.authority
                .orEmpty()
                .lowercase()

        val displayName =
            metadata.displayName
                .orEmpty()
                .lowercase()

        val relativePath =
            metadata.relativePath
                .orEmpty()
                .replace(
                    '\\',
                    '/'
                )
                .lowercase()

        val bucket =
            metadata.bucketDisplayName
                .orEmpty()
                .lowercase()

        val owner =
            metadata.ownerPackageName
                .orEmpty()
                .lowercase()

        val combinedPath =
            "$relativePath/$bucket"

        if (
            authority ==
            "$appPackageName.fileprovider"
                .lowercase() ||
            owner ==
            appPackageName.lowercase() ||
            combinedPath.contains(
                "/pictures/goldbrain"
            ) ||
            displayName.startsWith(
                "goldbrain_"
            )
        ) {
            return result(
                PhotoSourceType.GOLDBRAIN,
                100
            )
        }

        if (
            DOWNLOAD_PATH_MARKERS
                .any(
                    combinedPath::contains
                )
        ) {
            return result(
                PhotoSourceType.DOWNLOAD,
                98
            )
        }

        if (
            SCREENSHOT_PATH_MARKERS
                .any(
                    combinedPath::contains
                )
        ) {
            return result(
                PhotoSourceType.SCREENSHOT,
                98
            )
        }

        if (
            MESSENGER_MARKERS
                .any {
                    marker ->
                    combinedPath.contains(
                        marker
                    ) ||
                        owner.contains(
                            marker
                        )
                }
        ) {
            return result(
                PhotoSourceType.MESSENGER,
                94
            )
        }

        if (
            CAMERA_PATH_MARKERS
                .any(
                    combinedPath::contains
                )
        ) {
            return result(
                PhotoSourceType.CAMERA,
                95
            )
        }

        if (
            BROWSER_PACKAGE_MARKERS
                .any(
                    owner::contains
                )
        ) {
            return result(
                PhotoSourceType.DOWNLOAD,
                78
            )
        }

        if (
            CAMERA_PACKAGE_MARKERS
                .any(
                    owner::contains
                )
        ) {
            return result(
                PhotoSourceType.CAMERA,
                86
            )
        }

        if (
            SCREENSHOT_NAME_MARKERS
                .any(
                    displayName::contains
                )
        ) {
            return result(
                PhotoSourceType.SCREENSHOT,
                76
            )
        }

        if (
            CAMERA_NAME_PATTERNS
                .any {
                    it.matches(
                        displayName
                    )
                }
        ) {
            return result(
                PhotoSourceType.CAMERA,
                62
            )
        }

        return result(
            PhotoSourceType.UNKNOWN,
            0
        )
    }

    private fun result(
        type: PhotoSourceType,
        confidence: Int
    ) =
        PhotoSourceClassification(
            type = type,
            confidence = confidence
        )

    private companion object {
        val DOWNLOAD_PATH_MARKERS =
            listOf(
                "/download/",
                "/downloads/"
            )

        val SCREENSHOT_PATH_MARKERS =
            listOf(
                "/screenshots/",
                "/screenshot/",
                "/screen captures/",
                "/screen_capture/"
            )

        val CAMERA_PATH_MARKERS =
            listOf(
                "/dcim/camera/",
                "/dcim/100media/",
                "/dcim/100andro/"
            )

        val MESSENGER_MARKERS =
            listOf(
                "whatsapp",
                "telegram",
                "org.telegram.messenger",
                "signal",
                "org.thoughtcrime.securesms",
                "facebook.orca",
                "messenger",
                "viber",
                "snapchat"
            )

        val BROWSER_PACKAGE_MARKERS =
            listOf(
                "chrome",
                "firefox",
                "mozilla",
                "microsoft.emmx",
                "opera",
                "browser"
            )

        val CAMERA_PACKAGE_MARKERS =
            listOf(
                "camera",
                "googlecamera"
            )

        val SCREENSHOT_NAME_MARKERS =
            listOf(
                "screenshot",
                "screen_shot",
                "скриншот"
            )

        val CAMERA_NAME_PATTERNS =
            listOf(
                Regex(
                    """img[_-]?\d+.*"""
                ),
                Regex(
                    """pxl[_-]?\d+.*"""
                ),
                Regex(
                    """dsc[_-]?\d+.*"""
                )
            )
    }
}
