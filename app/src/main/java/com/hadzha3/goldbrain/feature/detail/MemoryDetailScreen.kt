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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hadzha3.goldbrain.R
import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.domain.facts.MemoryFact
import com.hadzha3.goldbrain.domain.facts.MemoryFactType
import com.hadzha3.goldbrain.domain.facts.MemoryFactsExtractor
import java.text.DateFormat
import java.util.Date

@Composable
fun MemoryDetailScreen(
    memory: MemoryEntity,
    onBack: () -> Unit,
    onOpenOriginal: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var note by
        rememberSaveable(
            memory.uri
        ) {
            mutableStateOf(
                memory.userNote
            )
        }
    val facts =
        remember(
            memory.ocrText,
            note
        ) {
            MemoryFactsExtractor
                .extract(
                    memory.ocrText,
                    note
                )
        }

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

        DetailSectionTitle(
            title = stringResource(
                R.string.detail_note
            )
        )

        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 6,
            placeholder = {
                Text(
                    stringResource(
                        R.string.detail_note_hint
                    )
                )
            }
        )

        Button(
            onClick = {
                onSaveNote(
                    note
                )
            },
            enabled =
                note.trim() !=
                    memory.userNote
        ) {
            Text(
                stringResource(
                    R.string.detail_note_save
                )
            )
        }

        if (facts.isNotEmpty()) {
            DetailSectionTitle(
                title = stringResource(
                    R.string.detail_facts
                )
            )

            facts.forEach { fact ->
                FactRow(
                    fact = fact
                )
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
private fun FactRow(
    fact: MemoryFact
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                2.dp
            )
    ) {
        Text(
            text =
                factTypeLabel(
                    fact.type
                ),
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            fontWeight =
                FontWeight.SemiBold
        )

        Text(
            text = fact.value,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

@Composable
private fun factTypeLabel(
    type: MemoryFactType
): String =
    when (type) {
        MemoryFactType.MONEY ->
            stringResource(
                R.string.fact_money
            )

        MemoryFactType.EMAIL ->
            stringResource(
                R.string.fact_email
            )

        MemoryFactType.PHONE ->
            stringResource(
                R.string.fact_phone
            )

        MemoryFactType.URL ->
            stringResource(
                R.string.fact_url
            )

        MemoryFactType.DATE ->
            stringResource(
                R.string.fact_date
            )

        MemoryFactType.ISBN ->
            stringResource(
                R.string.fact_isbn
            )
    }

@Composable
private fun DetailSectionTitle(
    title: String
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
