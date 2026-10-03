package com.hadzha3.goldbrain.domain.facts

object MemoryFactsExtractor {
    fun extract(
        vararg textParts: String
    ): List<MemoryFact> {
        val source =
            textParts
                .filter {
                    it.isNotBlank()
                }
                .joinToString("\n")

        if (source.isBlank()) {
            return emptyList()
        }

        val facts =
            buildList {
                extractRegex(
                    source,
                    EMAIL_REGEX,
                    MemoryFactType.EMAIL
                )

                extractRegex(
                    source,
                    URL_REGEX,
                    MemoryFactType.URL
                )

                extractPhones(
                    source
                )

                extractMoney(
                    source
                )

                extractRegex(
                    source,
                    NUMERIC_DATE_REGEX,
                    MemoryFactType.DATE
                )

                extractRegex(
                    source,
                    RUSSIAN_DATE_REGEX,
                    MemoryFactType.DATE
                )

                extractRegex(
                    source,
                    ISBN_REGEX,
                    MemoryFactType.ISBN
                )
            }

        return facts
            .map {
                it.copy(
                    value =
                        cleanup(
                            it.value
                        )
                )
            }
            .filter {
                it.value.isNotBlank()
            }
            .distinctBy {
                it.type to
                    it.value.lowercase()
            }
            .take(
                MAX_FACTS
            )
    }

    fun searchableText(
        facts: List<MemoryFact>
    ): String =
        facts
            .joinToString(" ") { fact ->
                buildString {
                    append(
                        fact.value
                    )
                    append(' ')
                    append(
                        aliases(
                            fact.type
                        )
                    )
                }
            }

    private fun MutableList<MemoryFact>
        .extractRegex(
            source: String,
            regex: Regex,
            type: MemoryFactType
        ) {
        regex
            .findAll(source)
            .forEach { match ->
                add(
                    MemoryFact(
                        type = type,
                        value =
                            match.value
                    )
                )
            }
    }

    private fun MutableList<MemoryFact>
        .extractPhones(
            source: String
        ) {
        PHONE_REGEX
            .findAll(source)
            .forEach { match ->
                val value =
                    cleanup(
                        match.value
                    )

                val digits =
                    value.count {
                        it.isDigit()
                    }

                if (
                    digits in
                        MIN_PHONE_DIGITS..
                            MAX_PHONE_DIGITS
                ) {
                    add(
                        MemoryFact(
                            type =
                                MemoryFactType.PHONE,
                            value = value
                        )
                    )
                }
            }
    }

    private fun MutableList<MemoryFact>
        .extractMoney(
            source: String
        ) {
        MONEY_AFTER_REGEX
            .findAll(source)
            .forEach { match ->
                add(
                    MemoryFact(
                        type =
                            MemoryFactType.MONEY,
                        value =
                            match.value
                    )
                )
            }

        MONEY_BEFORE_REGEX
            .findAll(source)
            .forEach { match ->
                add(
                    MemoryFact(
                        type =
                            MemoryFactType.MONEY,
                        value =
                            match.value
                    )
                )
            }
    }

    private fun cleanup(
        value: String
    ): String =
        value
            .trim()
            .trim(
                '.',
                ',',
                ';',
                ':',
                ')',
                ']',
                '}'
            )
            .replace(
                MULTISPACE_REGEX,
                " "
            )

    private fun aliases(
        type: MemoryFactType
    ): String =
        when (type) {
            MemoryFactType.MONEY ->
                "сумма цена стоимость сколько стоил стоила стоило amount price total money"

            MemoryFactType.EMAIL ->
                "почта email e-mail электронная адрес контакт contact"

            MemoryFactType.PHONE ->
                "телефон номер phone tel mobile мобильный контакт contact"

            MemoryFactType.URL ->
                "ссылка сайт url website web www"

            MemoryFactType.DATE ->
                "дата число когда date day"

            MemoryFactType.ISBN ->
                "isbn книга book издание edition"
        }

    private const val MAX_FACTS =
        24

    private const val MIN_PHONE_DIGITS =
        8

    private const val MAX_PHONE_DIGITS =
        15

    private val MULTISPACE_REGEX =
        Regex("\\s+")

    private val EMAIL_REGEX =
        Regex(
            """(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b"""
        )

    private val URL_REGEX =
        Regex(
            """(?i)\b(?:https?://|www\.)[^\s<>()]+"""
        )

    private val PHONE_REGEX =
        Regex(
            """(?<!\d)(?:\+?\d[\d\s().-]{6,}\d)(?!\d)"""
        )

    private val MONEY_AFTER_REGEX =
        Regex(
            """(?iu)(?<!\d)(?:\d{1,3}(?:[ \u00A0]\d{3})+(?:[.,]\d{1,2})?|\d+(?:[.,]\d{1,2})?)\s*(?:₽|руб(?:\.|ля|лей)?|р\.|₸|тг|kzt|\$|usd|€|eur|£|gbp)"""
        )

    private val MONEY_BEFORE_REGEX =
        Regex(
            """(?iu)(?:\$|usd|€|eur|£|gbp)\s*(?:\d{1,3}(?:[ \u00A0]\d{3})+(?:[.,]\d{1,2})?|\d+(?:[.,]\d{1,2})?)"""
        )

    private val NUMERIC_DATE_REGEX =
        Regex(
            """(?<!\d)(?:[0-3]?\d)[./-](?:[01]?\d)[./-](?:(?:19|20)?\d{2})(?!\d)"""
        )

    private val RUSSIAN_DATE_REGEX =
        Regex(
            """(?iu)\b(?:[0-3]?\d)\s+(?:января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)(?:\s+\d{4})?\b"""
        )

    private val ISBN_REGEX =
        Regex(
            """(?iu)\bISBN(?:-1[03])?\s*:?\s*[0-9X][0-9X\-\s]{8,20}[0-9X]\b"""
        )
}
