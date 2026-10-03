package com.hadzha3.goldbrain.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class LocalImageAnalyzer(
    private val context: Context
) {
    private val textRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val labeler =
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    data class Analysis(
        val title: String,
        val category: String,
        val text: String,
        val labels: List<String>
    )

    suspend fun analyze(uri: Uri): Analysis = coroutineScope {
        val image = InputImage.fromFilePath(context, uri)

        val textTask = async {
            runCatching {
                textRecognizer.process(image).await().text
            }.getOrDefault("")
        }

        val labelTask = async {
            runCatching {
                labeler.process(image).await()
                    .filter { it.confidence >= 0.60f }
                    .take(8)
                    .map { it.text }
            }.getOrDefault(emptyList())
        }

        val text = textTask.await().trim()
        val labels = labelTask.await()
        val category = classify(text, labels)
        val title = buildTitle(category, text, labels)

        Analysis(
            title = title,
            category = category,
            text = text,
            labels = labels
        )
    }

    private fun classify(text: String, labels: List<String>): String {
        val source = (text + " " + labels.joinToString(" ")).lowercase()

        return when {
            listOf(
                "total", "subtotal", "receipt", "invoice",
                "cash", "visa", "mastercard", "итого", "чек"
            ).any(source::contains) -> "Чек"

            listOf(
                "isbn", "publisher", "chapter", "book",
                "книга", "издательство"
            ).any(source::contains) -> "Книга"

            listOf(
                "concert", "festival", "ticket", "admission",
                "event", "концерт", "билет", "афиша"
            ).any(source::contains) -> "Событие"

            listOf(
                "email", "www.", "@", "phone", "tel",
                "linkedin", "телефон"
            ).any(source::contains) -> "Контакт"

            listOf(
                "car", "vehicle", "parking",
                "машина", "парковка"
            ).any(source::contains) -> "Парковка"

            else -> "Память"
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
}
