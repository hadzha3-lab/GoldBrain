package com.hadzha3.goldbrain

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CameraScreen(
                    onSaved = { uriString ->
                        setResult(
                            RESULT_OK,
                            Intent().setData(
                                android.net.Uri.parse(uriString)
                            )
                        )
                        finish()
                    },
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
private fun CameraScreen(
    onSaved: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: Executor = ContextCompat.getMainExecutor(context)

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var error by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            runCatching {
                val cameraProvider = providerFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                ready = true
            }.onFailure {
                error = it.message ?: "Не удалось открыть камеру"
            }
        }

        providerFuture.addListener(listener, executor)

        onDispose {
            if (providerFuture.isDone) {
                runCatching {
                    providerFuture.get().unbindAll()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            error?.let {
                Text(
                    text = it,
                    color = Color.White
                )
            }

            Button(
                enabled = ready,
                onClick = {
                    capturePhoto(
                        activity = context as ComponentActivity,
                        imageCapture = imageCapture,
                        executor = executor,
                        onSaved = onSaved,
                        onError = { message ->
                            error = message
                        }
                    )
                }
            ) {
                Text("Сфотографировать")
            }

            OutlinedButton(
                onClick = onClose
            ) {
                Text(
                    text = "Закрыть",
                    color = Color.White
                )
            }
        }
    }
}

private fun capturePhoto(
    activity: ComponentActivity,
    imageCapture: ImageCapture,
    executor: Executor,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    val name = "GoldBrain_${System.currentTimeMillis()}.jpg"

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/GoldBrain"
            )
        }
    }

    val output = ImageCapture.OutputFileOptions.Builder(
        activity.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    ).build()

    imageCapture.takePicture(
        output,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(
                outputFileResults: ImageCapture.OutputFileResults
            ) {
                val uri = outputFileResults.savedUri

                if (uri == null) {
                    onError("Фото сохранено, но Android не вернул его адрес")
                } else {
                    onSaved(uri.toString())
                }
            }

            override fun onError(
                exception: ImageCaptureException
            ) {
                onError(
                    exception.message ?: "Не удалось сохранить фото"
                )
            }
        }
    )
}
