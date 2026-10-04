package com.hadzha3.goldbrain.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.feature.source.isKnownPhotoSource
import com.hadzha3.goldbrain.feature.source.photoSourceLabel

@Composable
fun MemoryCard(
    item: MemoryEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (item.isAvailable) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                UnavailableThumbnail()
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text =
                        if (
                            isKnownPhotoSource(
                                item.sourceType
                            )
                        ) {
                            "${item.category} · ${photoSourceLabel(item.sourceType)}"
                        } else {
                            item.category
                        },
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium
                )

                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (
                    item.userNote
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            item.userNote,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        maxLines = 2,
                        overflow =
                            TextOverflow
                                .Ellipsis
                    )
                }

                if (!item.isAvailable) {
                    Text(
                        text = stringResource(
                            R.string.memory_original_unavailable
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                } else if (item.labels.isNotBlank()) {
                    Text(
                        text = item.labels,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun UnavailableThumbnail() {
    Card(
        modifier = Modifier.size(88.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "×",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
