package com.hadzha3.goldbrain.domain.index

object IndexTextCompactor {
    const val MAX_OCR_CHARS =
        2_048

    private const val OCR_HEAD_CHARS =
        1_536

    private const val LABEL_LIMIT =
        8

    private const val MAX_LABEL_CHARS =
        48

    private val MULTISPACE =
        Regex("[\\t \\u00A0]+")

    fun compactOcr(
        text: String
    ): String {
        val normalized =
            text
                .lineSequence()
                .map {
                    it.trim()
                        .replace(
                            MULTISPACE,
                            " "
                        )
                }
                .filter {
                    it.isNotEmpty()
                }
                .joinToString(
                    "\n"
                )

        if (
            normalized.length <=
            MAX_OCR_CHARS
        ) {
            return normalized
        }

        val separator =
            "\n…\n"

        val tailChars =
            MAX_OCR_CHARS -
                OCR_HEAD_CHARS -
                separator.length

        return buildString(
            MAX_OCR_CHARS
        ) {
            append(
                normalized.take(
                    OCR_HEAD_CHARS
                )
            )

            append(
                separator
            )

            append(
                normalized.takeLast(
                    tailChars
                )
            )
        }
    }

    fun compactLabels(
        labels: List<String>
    ): String =
        labels
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinctBy {
                it.lowercase()
            }
            .take(
                LABEL_LIMIT
            )
            .map {
                it.take(
                    MAX_LABEL_CHARS
                )
            }
            .joinToString(
                ", "
            )
}
