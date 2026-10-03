package com.hadzha3.goldbrain.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.core.permissions.GalleryAccessMode

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onChangeAccess: () -> Unit,
    onAutoIndexChange: (Boolean) -> Unit,
    onIndexNow: () -> Unit,
    onRecheckOriginals: () -> Unit,
    onRetryFailedPhotos: () -> Unit,
    onClearTemporaryCache: () -> Unit,
    onClearIndex: () -> Unit
) {
    val context =
        LocalContext.current
    var showClearConfirmation by
        remember {
            mutableStateOf(false)
        }

    Column(
        modifier = Modifier
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onBack
            ) {
                Text(
                    stringResource(
                        R.string.settings_back
                    )
                )
            }

            Text(
                text = stringResource(
                    R.string.settings_title
                ),
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Bold
            )
        }

        SettingsCard(
            title = stringResource(
                R.string.settings_access_title
            )
        ) {
            Text(
                text = accessText(
                    state.galleryAccessMode
                )
            )

            OutlinedButton(
                onClick =
                    onChangeAccess
            ) {
                Text(
                    stringResource(
                        R.string.settings_change_access
                    )
                )
            }
        }

        SettingsCard(
            title = stringResource(
                R.string.settings_background_title
            )
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                horizontalArrangement =
                    Arrangement
                        .SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(
                            R.string.settings_auto_index
                        ),
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Text(
                        text =
                            stringResource(
                                R.string.settings_auto_index_hint
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }

                Switch(
                    checked =
                        state
                            .autoIndexEnabled,
                    onCheckedChange =
                        onAutoIndexChange
                )
            }
        }

        SettingsCard(
            title = stringResource(
                R.string.settings_index_title
            )
        ) {
            Text(
                text =
                    stringResource(
                        R.string.settings_memory_count,
                        state.memoryCount
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string.settings_unavailable_count,
                        state.unavailableCount
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string.settings_failed_photo_count,
                        state.failedPhotoCount
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string.settings_index_size,
                        android.text.format.Formatter
                            .formatShortFileSize(
                                context,
                                state.indexBytes
                            )
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string.settings_cache_size,
                        android.text.format.Formatter
                            .formatShortFileSize(
                                context,
                                state.cacheBytes
                            )
                    )
            )

            if (
                state.isIndexing ||
                state.totalInRun > 0
            ) {
                LinearProgressIndicator(
                    progress = {
                        state.progressFraction
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Text(
                    text =
                        stringResource(
                            R.string.settings_index_progress,
                            state.indexedInRun,
                            state.totalInRun,
                            state.failedInRun
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }

            Button(
                onClick = onIndexNow,
                enabled =
                    state.galleryAccessMode !=
                        GalleryAccessMode.NONE
            ) {
                Text(
                    stringResource(
                        R.string.settings_index_now
                    )
                )
            }

            OutlinedButton(
                onClick =
                    onRecheckOriginals,
                enabled =
                    state.memoryCount > 0
            ) {
                Text(
                    stringResource(
                        R.string.settings_recheck
                    )
                )
            }

            OutlinedButton(
                onClick =
                    onRetryFailedPhotos,
                enabled =
                    state.failedPhotoCount > 0 &&
                        state.galleryAccessMode !=
                            GalleryAccessMode.NONE
            ) {
                Text(
                    stringResource(
                        R.string.settings_retry_failed
                    )
                )
            }

            OutlinedButton(
                onClick =
                    onClearTemporaryCache,
                enabled =
                    state.cacheBytes > 0L
            ) {
                Text(
                    stringResource(
                        R.string.settings_clear_cache
                    )
                )
            }
        }

        SettingsCard(
            title = stringResource(
                R.string.settings_privacy_title
            )
        ) {
            Text(
                text =
                    stringResource(
                        R.string.settings_privacy_text
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            OutlinedButton(
                onClick = {
                    showClearConfirmation =
                        true
                },
                enabled =
                    state.memoryCount > 0
            ) {
                Text(
                    stringResource(
                        R.string.settings_clear_index
                    )
                )
            }
        }

        Spacer(
            Modifier.height(24.dp)
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showClearConfirmation =
                    false
            },
            title = {
                Text(
                    stringResource(
                        R.string.settings_clear_confirm_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.settings_clear_confirm_text
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation =
                            false
                        onClearIndex()
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.settings_clear_confirm
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation =
                            false
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.settings_cancel
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            content()
        }
    }
}

@Composable
private fun accessText(
    mode: GalleryAccessMode
): String =
    when (mode) {
        GalleryAccessMode.FULL ->
            stringResource(
                R.string.settings_access_full
            )

        GalleryAccessMode.PARTIAL ->
            stringResource(
                R.string.settings_access_partial
            )

        GalleryAccessMode.NONE ->
            stringResource(
                R.string.settings_access_none
            )
    }
