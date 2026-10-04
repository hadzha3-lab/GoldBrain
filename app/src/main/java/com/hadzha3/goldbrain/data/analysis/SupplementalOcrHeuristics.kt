package com.hadzha3.goldbrain.data.analysis

object SupplementalOcrHeuristics {
    fun shouldRunCyrillicOcr(
        primaryText: String,
        labels: List<String>
    ): Boolean {
        val compactChars =
            primaryText.count {
                !it.isWhitespace()
            }

        val cyrillicChars =
            primaryText.count {
                it in 'А'..'я' ||
                    it == 'Ё' ||
                    it == 'ё'
            }

        if (
            cyrillicChars >=
            MIN_USEFUL_CYRILLIC_CHARS
        ) {
            return false
        }

        val labelText =
            labels
                .joinToString(" ")
                .lowercase()

        val looksTextHeavy =
            TEXT_LIKE_LABELS
                .any(
                    labelText::contains
                )

        if (looksTextHeavy) {
            return true
        }

        return compactChars >=
            MIN_PRIMARY_TEXT_FOR_FALLBACK
    }

    private const val MIN_PRIMARY_TEXT_FOR_FALLBACK =
        40

    private const val MIN_USEFUL_CYRILLIC_CHARS =
        40

    private val TEXT_LIKE_LABELS =
        listOf(
            "text",
            "font",
            "document",
            "paper",
            "book",
            "receipt",
            "invoice",
            "poster",
            "sign",
            "signage",
            "menu",
            "business card",
            "screenshot",
            "label",
            "advertising",
            "publication",
            "newspaper",
            "magazine"
        )
}
