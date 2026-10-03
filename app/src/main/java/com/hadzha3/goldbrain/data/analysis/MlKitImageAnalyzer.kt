package com.hadzha3.goldbrain.data.analysis

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hadzha3.goldbrain.domain.facts.MemoryFactType
import com.hadzha3.goldbrain.domain.facts.MemoryFactsExtractor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class MlKitImageAnalyzer(
    private val context: Context
) : ImageAnalyzer {
    private val textRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val labeler =
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    private val colorAnalyzer =
        DominantColorAnalyzer(
            context
        )

    private val cyrillicRecognizer =
        TesseractCyrillicRecognizer(
            context
        )

    override suspend fun analyze(
        uri: Uri
    ): ImageAnalysis = coroutineScope {
        val image = InputImage.fromFilePath(context, uri)

        val textDeferred = async {
            runCatching {
                textRecognizer.process(image).await().text
            }.getOrDefault("")
        }

        val labelsDeferred = async {
            runCatching {
                labeler.process(image).await()
                    .asSequence()
                    .filter {
                        it.confidence >=
                            MIN_LABEL_CONFIDENCE
                    }
                    .take(MAX_LABELS)
                    .map { it.text }
                    .toList()
            }.getOrDefault(
                emptyList()
            )
        }

        val colorsDeferred = async {
            runCatching {
                colorAnalyzer
                    .analyze(
                        uri
                    )
                    .flatMap {
                        it.labels()
                    }
            }.getOrDefault(
                emptyList()
            )
        }

        val primaryText =
            textDeferred
                .await()
                .trim()

        val primaryLabels =
            labelsDeferred.await()

        val cyrillicText =
            if (
                SupplementalOcrHeuristics
                    .shouldRunCyrillicOcr(
                        primaryText =
                            primaryText,
                        labels =
                            primaryLabels
                    )
            ) {
                runCatching {
                    cyrillicRecognizer
                        .recognize(
                            uri
                        )
                }.getOrDefault(
                    ""
                )
            } else {
                ""
            }

        val text =
            mergeText(
                primaryText,
                cyrillicText
            )

        val labels =
            (
                primaryLabels +
                    colorsDeferred.await()
            )
                .distinctBy {
                    it.lowercase()
                }
                .take(
                    MAX_TOTAL_LABELS
                )

        val category =
            classify(
                text,
                labels
            )

        ImageAnalysis(
            title = buildTitle(category, text, labels),
            category = category,
            text = text,
            labels = labels
        )
    }

    private fun mergeText(
        primary: String,
        supplemental: String
    ): String =
        sequenceOf(
            primary,
            supplemental
        )
            .flatMap {
                it.lineSequence()
            }
            .map {
                it.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .distinctBy {
                it.lowercase()
            }
            .joinToString(
                "\n"
            )

    private fun classify(
        text: String,
        labels: List<String>
    ): String {
        val source = buildString {
            append(text)
            append(' ')
            append(labels.joinToString(" "))
        }.lowercase()

        val facts =
            MemoryFactsExtractor
                .extract(
                    text
                )

        return when {
            RECEIPT_MARKERS.any(source::contains) ->
                "Чек"

            facts.any {
                it.type ==
                    MemoryFactType.ISBN
            } ||
                BOOK_MARKERS.any(source::contains) ->
                "Книга"

            EVENT_MARKERS.any(source::contains) ->
                "Событие"

            facts.any {
                it.type ==
                    MemoryFactType.EMAIL ||
                    it.type ==
                    MemoryFactType.PHONE
            } ||
                CONTACT_MARKERS.any(source::contains) ->
                "Контакт"

            PARKING_MARKERS.any(source::contains) ->
                "Парковка"

            else ->
                "Память"
        }
    }

    private fun buildTitle(
        category: String,
        text: String,
        labels: List<String>
    ): String {
        val firstUsefulLine = text
            .lineSequence()
            .map(String::trim)
            .firstOrNull { it.length in 3..70 }

        return firstUsefulLine
            ?: labels.take(3).joinToString(" · ").ifBlank { category }
    }

    private companion object {
        const val MIN_LABEL_CONFIDENCE = 0.60f
        const val MAX_LABELS = 8
        const val MAX_TOTAL_LABELS = 12

        val RECEIPT_MARKERS = listOf(
            "total", "subtotal", "receipt", "invoice",
            "cash", "visa", "mastercard", "итого", "чек"
        )
        val BOOK_MARKERS = listOf(
            "isbn", "publisher", "chapter", "book",
            "книга", "издательство"
        )
        val EVENT_MARKERS = listOf(
            "concert", "festival", "ticket", "admission",
            "event", "концерт", "билет", "афиша"
        )
        val CONTACT_MARKERS = listOf(
            "email", "www.", "@", "phone", "tel",
            "linkedin", "телефон"
        )
        val PARKING_MARKERS = listOf(
            "car", "vehicle", "parking",
            "машина", "парковка"
        )
    }
}
