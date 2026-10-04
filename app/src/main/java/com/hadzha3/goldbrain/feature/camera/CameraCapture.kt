package com.hadzha3.goldbrain.feature.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.Executor

object CameraCapture {
    fun saveNewCapture(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        onSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {
            saveNewMediaStoreCapture(
                context = context,
                imageCapture = imageCapture,
                executor = executor,
                onSaved = onSaved,
                onError = onError
            )
        } else {
            saveNewAppOwnedCapture(
                context = context,
                imageCapture = imageCapture,
                executor = executor,
                onSaved = onSaved,
                onError = onError
            )
        }
    }

    private fun saveNewMediaStoreCapture(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        onSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val values =
            ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName()
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/GoldBrain"
                )
            }

        val output =
            ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI,
                values
            )
                .build()

        takePicture(
            imageCapture = imageCapture,
            output = output,
            executor = executor,
            onSaved = { result ->
                result.savedUri
                    ?.let(onSaved)
                    ?: onError(
                        MissingSavedUriException()
                    )
            },
            onError = onError
        )
    }

    private fun saveNewAppOwnedCapture(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        onSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val directory =
            context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            )
                ?.resolve(
                    "GoldBrain"
                )
                ?: File(
                    context.filesDir,
                    "captured"
                )

        if (
            !directory.exists() &&
            !directory.mkdirs()
        ) {
            onError(
                CaptureDirectoryException()
            )
            return
        }

        val file =
            File(
                directory,
                fileName()
            )

        val output =
            ImageCapture.OutputFileOptions
                .Builder(
                    file
                )
                .build()

        takePicture(
            imageCapture = imageCapture,
            output = output,
            executor = executor,
            onSaved = {
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                onSaved(
                    uri
                )
            },
            onError = onError
        )
    }

    private fun takePicture(
        imageCapture: ImageCapture,
        output:
            ImageCapture.OutputFileOptions,
        executor: Executor,
        onSaved:
            (
                ImageCapture
                    .OutputFileResults
            ) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        imageCapture.takePicture(
            output,
            executor,
            object :
                ImageCapture
                    .OnImageSavedCallback {
                override fun onImageSaved(
                    result:
                        ImageCapture
                            .OutputFileResults
                ) {
                    onSaved(
                        result
                    )
                }

                override fun onError(
                    exception:
                        ImageCaptureException
                ) {
                    onError(
                        exception
                    )
                }
            }
        )
    }

    private fun fileName(): String =
        "GoldBrain_${System.currentTimeMillis()}.jpg"
}

class MissingSavedUriException :
    IllegalStateException(
        "Saved image URI is missing"
    )

class CaptureDirectoryException :
    IllegalStateException(
        "Could not create private capture directory"
    )
