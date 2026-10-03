package com.hadzha3.goldbrain.feature.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.util.concurrent.Executor

object CameraCapture {
    fun saveToGallery(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        onSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "GoldBrain_${System.currentTimeMillis()}.jpg"
            )
            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/jpeg"
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/GoldBrain"
                )
            }
        }

        val output = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ).build()

        imageCapture.takePicture(
            output,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(
                    result: ImageCapture.OutputFileResults
                ) {
                    result.savedUri
                        ?.let(onSaved)
                        ?: onError(
                            MissingSavedUriException()
                        )
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {
                    onError(exception)
                }
            }
        )
    }
}

class MissingSavedUriException :
    IllegalStateException("Saved image URI is missing")
