package com.hadzha3.goldbrain.feature.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hadzha3.goldbrain.core.permissions.MediaPermissions
import com.hadzha3.goldbrain.feature.camera.CameraActivity
import com.hadzha3.goldbrain.feature.detail.MemoryDetailScreen

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMemory by
        viewModel.selectedMemory.collectAsStateWithLifecycle()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(100)
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        viewModel.indexSelected(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            viewModel.indexSelected(listOf(uri))
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                cameraLauncher.launch(
                    Intent(
                        context,
                        CameraActivity::class.java
                    )
                )
            }
        }

    val galleryPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (MediaPermissions.hasGalleryAccess(context)) {
                viewModel.startGalleryIndex()
                viewModel.enablePeriodicGalleryIndex()
            }
        }

    LaunchedEffect(Unit) {
        if (MediaPermissions.hasGalleryAccess(context)) {
            viewModel.enablePeriodicGalleryIndex()
        }
    }

    val memory = selectedMemory
    if (memory != null) {
        MemoryDetailScreen(
            memory = memory,
            onBack = viewModel::closeMemory,
            onOpenOriginal = {
                openOriginal(
                    context = context,
                    uri = Uri.parse(memory.uri)
                )
            }
        )
        return
    }

    HomeScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onMemoryClick = {
            viewModel.openMemory(it.uri)
        },
        onGalleryClick = {
            if (MediaPermissions.hasGalleryAccess(context)) {
                viewModel.startGalleryIndex()
                viewModel.enablePeriodicGalleryIndex()
            } else {
                galleryPermissionLauncher.launch(
                    MediaPermissions.requiredGalleryPermissions()
                )
            }
        },
        onCameraClick = {
            if (MediaPermissions.hasCameraAccess(context)) {
                cameraLauncher.launch(
                    Intent(
                        context,
                        CameraActivity::class.java
                    )
                )
            } else {
                cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        },
        onPickPhotosClick = {
            photoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    )
}

private fun openOriginal(
    context: android.content.Context,
    uri: Uri
) {
    val mimeType =
        context.contentResolver.getType(uri) ?: "image/*"

    val intent =
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                uri,
                mimeType
            )
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

    runCatching {
        context.startActivity(intent)
    }
}
