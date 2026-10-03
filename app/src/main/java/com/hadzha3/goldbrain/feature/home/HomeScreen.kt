package com.hadzha3.goldbrain.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.feature.home.components.MemoryCard

@Composable
fun HomeScreen(
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onMemoryClick: (MemoryEntity) -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    cameraAvailable: Boolean,
    onPickPhotosClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPickPhotosClick,
                text = {
                    Text(
                        stringResource(
                            R.string.home_choose_photo
                        )
                    )
                },
                icon = {
                    Text("＋")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(
                    horizontal = 16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement
                        .SpaceBetween
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.app_name
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .headlineMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                TextButton(
                    onClick =
                        onSettingsClick
                ) {
                    Text(
                        stringResource(
                            R.string.home_settings
                        )
                    )
                }
            }

            Text(
                text = buildString {
                    append(
                        pluralStringResource(
                            R.plurals.memory_count,
                            state.memoryCount,
                            state.memoryCount
                        )
                    )
                    append(" · ")
                    append(
                        stringResource(
                            R.string.home_originals_hint
                        )
                    )
                },
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            HomeActions(
                onGalleryClick =
                    onGalleryClick,
                onCameraClick =
                    onCameraClick,
                cameraAvailable =
                    cameraAvailable
            )

            Text(
                text =
                    stringResource(
                        R.string.home_gallery_hint
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )

            OutlinedTextField(
                value = state.query,
                onValueChange =
                    onQueryChange,
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(
                        stringResource(
                            R.string.home_search_label
                        )
                    )
                },
                placeholder = {
                    Text(
                        stringResource(
                            R.string.home_search_hint
                        )
                    )
                }
            )

            if (
                state.isIndexing ||
                state.statusText
                    .isNotBlank()
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            state.statusText
                        )
                    }
                )
            }

            HomeContent(
                state = state,
                onMemoryClick =
                    onMemoryClick
            )
        }
    }
}

@Composable
private fun HomeActions(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    cameraAvailable: Boolean
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        Button(
            onClick =
                onGalleryClick,
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                stringResource(
                    R.string.home_gallery
                )
            )
        }

        OutlinedButton(
            onClick =
                onCameraClick,
            enabled =
                cameraAvailable,
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                stringResource(
                    R.string.home_camera
                )
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onMemoryClick:
        (MemoryEntity) -> Unit
) {
    if (
        state.memories.isEmpty()
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                if (
                    state.query
                        .isBlank()
                ) {
                    stringResource(
                        R.string.home_empty
                    )
                } else {
                    stringResource(
                        R.string.home_nothing_found
                    )
                }
            )
        }
        return
    }

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),
        contentPadding =
            PaddingValues(
                bottom = 96.dp
            )
    ) {
        items(
            items = state.memories,
            key = {
                it.uri
            }
        ) { memory ->
            MemoryCard(
                item = memory,
                onClick = {
                    onMemoryClick(
                        memory
                    )
                }
            )
        }
    }
}
