package com.hadzha3.goldbrain.feature.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.hadzha3.goldbrain.R

@Composable
fun CameraScreen(
    onSaved: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(
                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            )
            .build()
    }

    var isReady by remember {
        mutableStateOf(false)
    }

    var errorText by remember {
        mutableStateOf<String?>(null)
    }

    var isCapturing by remember {
        mutableStateOf(false)
    }

    val cameraOpenError =
        stringResource(R.string.camera_open_error)
    val cameraSaveError =
        stringResource(R.string.camera_save_error)
    val cameraUriError =
        stringResource(R.string.camera_uri_error)

    DisposableEffect(
        lifecycleOwner,
        previewView,
        imageCapture
    ) {
        val providerFuture =
            ProcessCameraProvider.getInstance(context)

        providerFuture.addListener(
            {
                runCatching {
                    val provider = providerFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider =
                                previewView.surfaceProvider
                        }

                    val cameraSelector =
                        when {
                            provider.hasCamera(
                                CameraSelector
                                    .DEFAULT_BACK_CAMERA
                            ) ->
                                CameraSelector
                                    .DEFAULT_BACK_CAMERA

                            provider.hasCamera(
                                CameraSelector
                                    .DEFAULT_FRONT_CAMERA
                            ) ->
                                CameraSelector
                                    .DEFAULT_FRONT_CAMERA

                            else ->
                                error(
                                    cameraOpenError
                                )
                        }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    isReady = true
                }.onFailure {
                    errorText =
                        it.message ?: cameraOpenError
                }
            },
            executor
        )

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

        CameraControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            isReady = isReady,
            isCapturing = isCapturing,
            errorText = errorText,
            onCapture = {
                if (isCapturing) {
                    return@CameraControls
                }

                isCapturing = true
                errorText = null

                CameraCapture.saveNewCapture(
                    context = context,
                    imageCapture = imageCapture,
                    executor = executor,
                    onSaved = { uri ->
                        onSaved(
                            uri
                        )
                    },
                    onError = { error ->
                        isCapturing = false

                        errorText = when (error) {
                            is MissingSavedUriException ->
                                cameraUriError

                            else ->
                                error.message ?: cameraSaveError
                        }
                    }
                )
            },
            onClose = onClose
        )
    }
}

@Composable
private fun CameraControls(
    modifier: Modifier,
    isReady: Boolean,
    isCapturing: Boolean,
    errorText: String?,
    onCapture: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        errorText?.let {
            Text(
                text = it,
                color = Color.White
            )
        }

        Button(
            enabled =
                isReady &&
                    !isCapturing,
            onClick = onCapture
        ) {
            Text(
                stringResource(
                    if (isCapturing) {
                        R.string.camera_saving
                    } else {
                        R.string.camera_capture
                    }
                )
            )
        }

        OutlinedButton(
            enabled =
                !isCapturing,
            onClick = onClose
        ) {
            Text(
                text = stringResource(
                    R.string.camera_close
                ),
                color = Color.White
            )
        }
    }
}
