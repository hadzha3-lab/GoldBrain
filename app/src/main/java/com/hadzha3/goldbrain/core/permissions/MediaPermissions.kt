package com.hadzha3.goldbrain.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object MediaPermissions {
    fun requiredGalleryPermissions(): Array<String> =
        when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )

            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )

            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

    fun hasGalleryAccess(
        context: Context
    ): Boolean =
        when {
            Build.VERSION.SDK_INT >= 34 ->
                isGranted(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) || isGranted(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )

            Build.VERSION.SDK_INT >= 33 ->
                isGranted(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                )

            else ->
                isGranted(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
        }

    fun hasCameraAccess(
        context: Context
    ): Boolean =
        isGranted(
            context,
            Manifest.permission.CAMERA
        )

    private fun isGranted(
        context: Context,
        permission: String
    ): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
