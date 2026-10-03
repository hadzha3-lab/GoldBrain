package com.hadzha3.goldbrain.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class GalleryAccessMode {
    FULL,
    PARTIAL,
    NONE
}

object MediaPermissions {
    fun requiredGalleryPermissions(): Array<String> =
        when {
            Build.VERSION.SDK_INT >= 34 ->
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )

            Build.VERSION.SDK_INT >= 33 ->
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )

            else ->
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
        }

    fun galleryAccessMode(
        context: Context
    ): GalleryAccessMode =
        when {
            Build.VERSION.SDK_INT >= 34 -> {
                when {
                    isGranted(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) ->
                        GalleryAccessMode.FULL

                    isGranted(
                        context,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) ->
                        GalleryAccessMode.PARTIAL

                    else ->
                        GalleryAccessMode.NONE
                }
            }

            Build.VERSION.SDK_INT >= 33 -> {
                if (
                    isGranted(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    GalleryAccessMode.FULL
                } else {
                    GalleryAccessMode.NONE
                }
            }

            else -> {
                if (
                    isGranted(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    GalleryAccessMode.FULL
                } else {
                    GalleryAccessMode.NONE
                }
            }
        }

    fun hasGalleryAccess(
        context: Context
    ): Boolean =
        galleryAccessMode(context) !=
            GalleryAccessMode.NONE

    fun requiredCameraPermissions(): Array<String> =
        if (
            Build.VERSION.SDK_INT <=
            Build.VERSION_CODES.P
        ) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA
            )
        }

    fun hasCameraAccess(
        context: Context
    ): Boolean =
        requiredCameraPermissions()
            .all { permission ->
                isGranted(
                    context,
                    permission
                )
            }

    private fun isGranted(
        context: Context,
        permission: String
    ): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
