package com.hadzha3.goldbrain.feature.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.domain.source.PhotoSourceType

@Composable
fun photoSourceLabel(
    storedType: String
): String =
    when (
        PhotoSourceType
            .fromStored(
                storedType
            )
    ) {
        PhotoSourceType.CAMERA ->
            stringResource(
                R.string.photo_source_camera
            )

        PhotoSourceType.DOWNLOAD ->
            stringResource(
                R.string.photo_source_download
            )

        PhotoSourceType.SCREENSHOT ->
            stringResource(
                R.string.photo_source_screenshot
            )

        PhotoSourceType.MESSENGER ->
            stringResource(
                R.string.photo_source_messenger
            )

        PhotoSourceType.GOLDBRAIN ->
            stringResource(
                R.string.photo_source_goldbrain
            )

        PhotoSourceType.UNKNOWN ->
            stringResource(
                R.string.photo_source_unknown
            )
    }

fun isKnownPhotoSource(
    storedType: String
): Boolean =
    PhotoSourceType
        .fromStored(
            storedType
        ) !=
        PhotoSourceType.UNKNOWN
