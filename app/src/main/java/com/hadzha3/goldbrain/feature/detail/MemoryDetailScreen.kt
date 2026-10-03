package com.hadzha3.goldbrain.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.data.local.MemoryEntity
import java.text.DateFormat
import java.util.Date

@Composable
fun MemoryDetailScreen(
    memory: MemoryEntity,
    onBack: () -> Unit,
    onOpenOriginal: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack
        ) {
            Text(
                stringResource(R.string.detail_back)
            )
        }

        Text(
            text = memory.category,
            style = MaterialTheme.typography.labelLarge
        )

        Text(
            text = memory.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = DateFormat
                .getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
                )
                .format(Date(memory.createdAt)),
            style = MaterialTheme.typography.bodySmall
        )

        if (memory.isAvailable) {
            AsyncImage(
                model = memory.uri,
                contentDescription = memory.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Fit
            )

            Button(
                onClick = onOpenOriginal
            ) {
                Text(
                    stringResource(
                        R.string.detail_open_original
                    )
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.detail_original_unavailable
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(
                            R.string.detail_original_unavailable_hint
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (memory.labels.isNotBlank()) {
            DetailSection(
                title = stringResource(
                    R.string.detail_labels
                ),
                value = memory.labels
            )
        }

        if (memory.ocrText.isNotBlank()) {
            DetailSection(
                title = stringResource(
                    R.string.detail_recognized_text
                ),
                value = memory.ocrText
            )
        }

        Spacer(
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
