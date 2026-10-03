package com.hadzha3.goldbrain.data.analysis

object SupplementalOcrHeuristics {
    fun shouldRunCyrillicOcr(
        primaryText: String,
        labels: List<String>
    ): Boolean {
        if (
            primaryText
                .count {
                    !it.isWhitespace()
                } >=
            MIN_PRIMARY_TEXT_CHARS
        ) {
            return true
        }

        val labelText =
            labels
                .joinToString(" ")
                .lowercase()

        return TEXT_LIKE_LABELS
            .any(
                labelText::contains
            )
    }

    private const val MIN_PRIMARY_TEXT_CHARS =
        3

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
