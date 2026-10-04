package com.hadzha3.goldbrain.domain.source

enum class PhotoSourceType {
    CAMERA,
    DOWNLOAD,
    SCREENSHOT,
    MESSENGER,
    GOLDBRAIN,
    UNKNOWN;

    companion object {
        fun fromStored(
            value: String
        ): PhotoSourceType =
            entries.firstOrNull {
                it.name == value
            } ?: UNKNOWN
    }
}

data class PhotoSourceClassification(
    val type: PhotoSourceType,
    val confidence: Int
) {
    init {
        require(
            confidence in 0..100
        )
    }
}

data class PhotoSourceMetadata(
    val authority: String? = null,
    val displayName: String? = null,
    val relativePath: String? = null,
    val bucketDisplayName: String? = null,
    val ownerPackageName: String? = null
)
