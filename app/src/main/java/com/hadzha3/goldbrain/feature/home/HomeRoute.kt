package com.hadzha3.goldbrain.feature.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hadzha3.goldbrain.core.permissions.GalleryAccessMode
import com.hadzha3.goldbrain.core.permissions.MediaPermissions
import com.hadzha3.goldbrain.feature.camera.CameraActivity
import com.hadzha3.goldbrain.feature.detail.MemoryDetailScreen
import com.hadzha3.goldbrain.feature.settings.SettingsScreen
import com.hadzha3.goldbrain.feature.settings.SettingsViewModel

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel =
        viewModel(),
    settingsViewModel: SettingsViewModel =
        viewModel()
) {
    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val state by
        homeViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val selectedMemory by
        homeViewModel
            .selectedMemory
            .collectAsStateWithLifecycle()

    val detailActionState by
        homeViewModel
            .detailActionState
            .collectAsStateWithLifecycle()

    val settingsState by
        settingsViewModel
            .uiState
            .collectAsStateWithLifecycle()

    var showSettings by
        rememberSaveable {
            mutableStateOf(
                false
            )
        }

    var showGalleryAccessInfo by
        rememberSaveable {
            mutableStateOf(
                false
            )
        }

    val photoPicker =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .PickMultipleVisualMedia(
                        100
                    )
        ) { uris ->
            uris.forEach { uri ->
                runCatching {
                    context
                        .contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent
                                .FLAG_GRANT_READ_URI_PERMISSION
                        )
                }
            }

            homeViewModel
                .indexSelected(
                    uris
                )
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .StartActivityForResult()
        ) { result ->
            result.data
                ?.data
                ?.let { uri ->
                    homeViewModel
                        .indexSelected(
                            listOf(
                                uri
                            )
                        )
                }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) {
            if (
                MediaPermissions
                    .hasCameraAccess(
                        context
                    )
            ) {
                cameraLauncher.launch(
                    Intent(
                        context,
                        CameraActivity::class.java
                    )
                )
            }
        }

    val homeGalleryPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) {
            settingsViewModel
                .onPermissionsChanged()

            if (
                MediaPermissions
                    .hasGalleryAccess(
                        context
                    )
            ) {
                homeViewModel
                    .startGalleryIndex()

                homeViewModel
                    .enablePeriodicGalleryIndex()
            } else {
                homeViewModel
                    .showGalleryPermissionDenied()
            }
        }

    val settingsAccessPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) {
            settingsViewModel
                .onPermissionsChanged()
        }

    val settingsIndexPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) {
            settingsViewModel
                .onPermissionsChanged()

            if (
                MediaPermissions
                    .hasGalleryAccess(
                        context
                    )
            ) {
                settingsViewModel
                    .startIndexing()
            }
        }

    LaunchedEffect(Unit) {
        settingsViewModel
            .refreshPermissions()

        if (
            MediaPermissions
                .hasGalleryAccess(
                    context
                )
        ) {
            homeViewModel
                .enablePeriodicGalleryIndex()
        }
    }

    DisposableEffect(
        lifecycleOwner,
        context
    ) {
        val observer =
            LifecycleEventObserver {
                    _,
                    event ->
                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {
                    settingsViewModel
                        .onPermissionsChanged()

                    homeViewModel
                        .enablePeriodicGalleryIndex()
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        onDispose {
            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )
        }
    }


    LaunchedEffect(
        showSettings
    ) {
        if (showSettings) {
            settingsViewModel
                .refreshPermissions()

            settingsViewModel
                .refreshStorage()
        }
    }

    val memory =
        selectedMemory

    if (memory != null) {
        MemoryDetailScreen(
            memory = memory,
            actionState =
                detailActionState,
            onBack =
                homeViewModel::closeMemory,
            onOpenOriginal = {
                openOriginal(
                    context = context,
                    uri = Uri.parse(
                        memory.uri
                    )
                )
            },
            onSaveNote = { note ->
                homeViewModel
                    .updateMemoryNote(
                        uri = memory.uri,
                        note = note
                    )
            },
            onReanalyze = {
                homeViewModel
                    .reanalyzeMemory(
                        memory
                    )
            },
            onRemove = {
                homeViewModel
                    .removeMemory(
                        memory.uri
                    )
            }
        )
        return
    }

    if (showSettings) {
        SettingsScreen(
            state = settingsState,
            onBack = {
                showSettings =
                    false
            },
            onChangeAccess = {
                settingsAccessPermissionLauncher
                    .launch(
                        MediaPermissions
                            .requiredGalleryPermissions()
                    )
            },
            onAutoIndexChange =
                settingsViewModel::
                    setAutoIndexEnabled,
            onIndexNow = {
                if (
                    settingsState
                        .galleryAccessMode ==
                    GalleryAccessMode.NONE
                ) {
                    settingsIndexPermissionLauncher
                        .launch(
                            MediaPermissions
                                .requiredGalleryPermissions()
                        )
                } else {
                    settingsViewModel
                        .startIndexing()
                }
            },
            onRecheckOriginals =
                settingsViewModel::
                    recheckOriginals,
            onRetryFailedPhotos =
                settingsViewModel::
                    retryFailedPhotos,
            onOptimizeIndex =
                settingsViewModel::
                    optimizeIndex,
            onClearTemporaryCache =
                settingsViewModel::
                    clearTemporaryCache,
            onClearIndex =
                settingsViewModel::
                    clearIndex
        )
        return
    }

    HomeScreen(
        state = state,
        onQueryChange =
            homeViewModel::
                onQueryChange,
        onMemoryClick = {
            homeViewModel
                .openMemory(
                    it.uri
                )
        },
        onGalleryClick = {
            if (
                MediaPermissions
                    .hasGalleryAccess(
                        context
                    )
            ) {
                homeViewModel
                    .startGalleryIndex()

                homeViewModel
                    .enablePeriodicGalleryIndex()
            } else {
                showGalleryAccessInfo =
                    true
            }
        },
        onCameraClick = {
            if (
                MediaPermissions
                    .hasCameraHardware(
                        context
                    )
            ) {
                if (
                    MediaPermissions
                        .hasCameraAccess(
                            context
                        )
                ) {
                    cameraLauncher.launch(
                        Intent(
                            context,
                            CameraActivity::class.java
                        )
                    )
                } else {
                    cameraPermissionLauncher
                        .launch(
                            MediaPermissions
                                .requiredCameraPermissions()
                        )
                }
            }
        },
        cameraAvailable =
            MediaPermissions
                .hasCameraHardware(
                    context
                ),
        onPickPhotosClick = {
            photoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts
                        .PickVisualMedia
                        .ImageOnly
                )
            )
        },
        onSettingsClick = {
            showSettings =
                true
        }
    )

    if (showGalleryAccessInfo) {
        AlertDialog(
            onDismissRequest = {
                showGalleryAccessInfo =
                    false
            },
            title = {
                Text(
                    stringResource(
                        R.string.gallery_access_info_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.gallery_access_info_text
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGalleryAccessInfo =
                            false

                        homeGalleryPermissionLauncher
                            .launch(
                                MediaPermissions
                                    .requiredGalleryPermissions()
                            )
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.gallery_access_continue
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGalleryAccessInfo =
                            false
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.gallery_access_cancel
                        )
                    )
                }
            }
        )
    }
}

private fun openOriginal(
    context: android.content.Context,
    uri: Uri
) {
    val mimeType =
        context
            .contentResolver
            .getType(
                uri
            ) ?: "image/*"

    val intent =
        Intent(
            Intent.ACTION_VIEW
        )
            .setDataAndType(
                uri,
                mimeType
            )
            .addFlags(
                Intent
                    .FLAG_GRANT_READ_URI_PERMISSION
            )

    runCatching {
        context.startActivity(
            intent
        )
    }
}
