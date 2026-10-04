package com.hadzha3.goldbrain.domain.search

import com.hadzha3.goldbrain.data.local.MemoryEntity
import com.hadzha3.goldbrain.domain.facts.MemoryFactsExtractor
import java.text.Normalizer
import java.util.Calendar

class MemorySearchEngine(
    private val nowProvider: () -> Long = System::currentTimeMillis
) {
    private val factSearchCache =
        object : LinkedHashMap<String, CachedFactSearch>(128, 0.75f, true) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<String, CachedFactSearch>?
            ): Boolean =
                size > MAX_FACT_CACHE_SIZE
        }

    fun search(
        items: List<MemoryEntity>,
        query: String
    ): List<MemoryEntity> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return items

        val temporalFilter =
            buildTemporalFilter(normalizedQuery)

        val words = normalizedQuery
            .split(WHITESPACE)
            .asSequence()
            .map(String::trim)
            .filter { it.length >= MIN_TERM_LENGTH }
            .filterNot(STOP_WORDS::contains)
            .filterNot(TEMPORAL_WORDS::contains)
            .distinct()
            .toList()

        val dateFiltered =
            if (temporalFilter == null) {
                items
            } else {
                items.filter { item ->
                    item.createdAt in temporalFilter
                }
            }

        if (words.isEmpty()) {
            return dateFiltered
                .sortedByDescending {
                    it.createdAt
                }
        }

        return dateFiltered
            .mapNotNull { item ->
                score(
                    item = item,
                    normalizedQuery = normalizedQuery,
                    words = words
                )?.let { score ->
                    item to score
                }
            }
            .sortedWith(
                compareByDescending<Pair<MemoryEntity, Int>> {
                    it.second
                }.thenByDescending {
                    it.first.createdAt
                }
            )
            .map {
                it.first
            }
    }

    private fun score(
        item: MemoryEntity,
        normalizedQuery: String,
        words: List<String>
    ): Int? {
        val category =
            normalize(item.category)

        val factSearchText =
            factsSearchText(item)

        val normalizedFactSearchText =
            normalize(factSearchText)

        val haystack =
            normalize(
                listOf(
                    item.category,
                    item.title,
                    item.ocrText,
                    item.labels,
                    item.userNote,
                    factSearchText
                ).joinToString(" ")
            )

        val hits = words.count { term ->
            matches(
                haystack = haystack,
                term = term
            )
        }

        if (hits == 0) return null

        val exactPhraseBonus =
            if (
                words.size > 1 &&
                haystack.contains(normalizedQuery)
            ) {
                EXACT_PHRASE_BONUS
            } else {
                0
            }

        val categoryBonus =
            if (
                words.any { term ->
                    matches(
                        haystack = category,
                        term = term
                    )
                }
            ) {
                CATEGORY_BONUS
            } else {
                0
            }

        val factBonus =
            words.count { term ->
                matches(
                    haystack =
                        normalizedFactSearchText,
                    term = term
                )
            } * FACT_BONUS

        return hits * TERM_SCORE +
            exactPhraseBonus +
            categoryBonus +
            factBonus
    }

    private fun matches(
        haystack: String,
        term: String
    ): Boolean {
        if (
            haystack.contains(
                term
            )
        ) {
            return true
        }

        return synonymsFor(
            term
        ).any(
            haystack::contains
        )
    }

    private fun synonymsFor(
        term: String
    ): List<String> {
        val exact =
            SYNONYMS[
                term
            ].orEmpty()

        val stemmed =
            STEM_SYNONYMS
                .asSequence()
                .filter {
                    term.startsWith(
                        it.key
                    )
                }
                .flatMap {
                    it.value
                        .asSequence()
                }
                .toList()

        return (
            exact +
                stemmed
            ).distinct()
    }

    private fun factsSearchText(
        item: MemoryEntity
    ): String {
        val signature =
            31 * item.ocrText.hashCode() +
                item.userNote.hashCode()

        synchronized(
            factSearchCache
        ) {
            val cached =
                factSearchCache[
                    item.uri
                ]

            if (
                cached != null &&
                cached.signature ==
                    signature
            ) {
                return cached.text
            }
        }

        val text =
            MemoryFactsExtractor
                .searchableText(
                    MemoryFactsExtractor
                        .extract(
                            item.ocrText,
                            item.userNote
                        )
                )

        synchronized(
            factSearchCache
        ) {
            factSearchCache[
                item.uri
            ] =
                CachedFactSearch(
                    signature =
                        signature,
                    text = text
                )
        }

        return text
    }

    private fun buildTemporalFilter(
        query: String
    ): LongRange? {
        val now =
            nowProvider()

        return when {
            containsAny(
                query,
                "позавчера",
                "day before yesterday"
            ) -> dayRange(
                now = now,
                daysAgo = 2
            )

            containsAny(
                query,
                "вчера",
                "yesterday"
            ) -> dayRange(
                now = now,
                daysAgo = 1
            )

            containsAny(
                query,
                "сегодня",
                "today"
            ) -> dayRange(
                now = now,
                daysAgo = 0
            )

            containsAny(
                query,
                "на этой неделе",
                "эта неделя",
                "this week"
            ) -> currentWeekRange(now)

            containsAny(
                query,
                "в этом месяце",
                "этот месяц",
                "this month"
            ) -> currentMonthRange(now)

            containsAny(
                query,
                "летом",
                "summer"
            ) -> mostRecentSummerRange(now)

            else -> null
        }
    }

    private fun dayRange(
        now: Long,
        daysAgo: Int
    ): LongRange {
        val start =
            calendarAtStartOfDay(now).apply {
                add(
                    Calendar.DAY_OF_YEAR,
                    -daysAgo
                )
            }

        val end =
            start.clone() as Calendar

        end.add(
            Calendar.DAY_OF_YEAR,
            1
        )

        return start.timeInMillis until
            end.timeInMillis
    }

    private fun currentWeekRange(
        now: Long
    ): LongRange {
        val start =
            calendarAtStartOfDay(now)

        val dayOfWeek =
            start.get(
                Calendar.DAY_OF_WEEK
            )

        val deltaFromMonday =
            (dayOfWeek -
                Calendar.MONDAY +
                DAYS_IN_WEEK) %
                DAYS_IN_WEEK

        start.add(
            Calendar.DAY_OF_YEAR,
            -deltaFromMonday
        )

        val end =
            start.clone() as Calendar

        end.add(
            Calendar.DAY_OF_YEAR,
            DAYS_IN_WEEK
        )

        return start.timeInMillis until
            end.timeInMillis
    }

    private fun currentMonthRange(
        now: Long
    ): LongRange {
        val start =
            calendarAtStartOfDay(now)

        start.set(
            Calendar.DAY_OF_MONTH,
            1
        )

        val end =
            start.clone() as Calendar

        end.add(
            Calendar.MONTH,
            1
        )

        return start.timeInMillis until
            end.timeInMillis
    }

    private fun mostRecentSummerRange(
        now: Long
    ): LongRange {
        val current =
            calendarAtStartOfDay(now)

        val currentMonth =
            current.get(
                Calendar.MONTH
            )

        val summerYear =
            if (
                currentMonth <
                Calendar.JUNE
            ) {
                current.get(
                    Calendar.YEAR
                ) - 1
            } else {
                current.get(
                    Calendar.YEAR
                )
            }

        val start =
            Calendar.getInstance()
                .apply {
                    clear()
                    set(
                        summerYear,
                        Calendar.JUNE,
                        1,
                        0,
                        0,
                        0
                    )
                }

        val end =
            Calendar.getInstance()
                .apply {
                    clear()
                    set(
                        summerYear,
                        Calendar.SEPTEMBER,
                        1,
                        0,
                        0,
                        0
                    )
                }

        return start.timeInMillis until
            end.timeInMillis
    }

    private fun calendarAtStartOfDay(
        time: Long
    ): Calendar =
        Calendar.getInstance()
            .apply {
                timeInMillis = time
                set(
                    Calendar.HOUR_OF_DAY,
                    0
                )
                set(
                    Calendar.MINUTE,
                    0
                )
                set(
                    Calendar.SECOND,
                    0
                )
                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

    private fun containsAny(
        value: String,
        vararg variants: String
    ): Boolean =
        variants.any(
            value::contains
        )

    private fun normalize(
        value: String
    ): String =
        Normalizer.normalize(
            value.lowercase(),
            Normalizer.Form.NFKC
        )
            .replace('ё', 'е')
            .replace(
                NON_SEARCHABLE,
                " "
            )
            .replace(
                WHITESPACE,
                " "
            )
            .trim()

    private data class CachedFactSearch(
        val signature: Int,
        val text: String
    )

    private companion object {
        const val MIN_TERM_LENGTH = 2
        const val TERM_SCORE = 5
        const val EXACT_PHRASE_BONUS = 10
        const val CATEGORY_BONUS = 3
        const val FACT_BONUS = 4
        const val DAYS_IN_WEEK = 7
        const val MAX_FACT_CACHE_SIZE = 512

        val WHITESPACE =
            Regex("\\s+")

        val NON_SEARCHABLE =
            Regex(
                "[^\\p{L}\\p{N}@._-]+"
            )

        val STOP_WORDS = setOf(
            "найди",
            "найти",
            "покажи",
            "фото",
            "фотку",
            "фотографию",
            "фотографировал",
            "фотографировала",
            "снимал",
            "снимала",
            "мне",
            "мой",
            "моя",
            "мою",
            "как",
            "называлась",
            "назывался",
            "который",
            "которую",
            "где",
            "из",
            "от",
            "на",
            "в",
            "во",
            "и",
            "я",
            "the",
            "a",
            "an",
            "find",
            "show",
            "photo",
            "picture"
        )

        val TEMPORAL_WORDS = setOf(
            "сегодня",
            "вчера",
            "позавчера",
            "летом",
            "неделе",
            "неделя",
            "месяце",
            "месяц",
            "этой",
            "этом",
            "этот",
            "today",
            "yesterday",
            "summer",
            "week",
            "month",
            "this",
            "day",
            "before"
        )

        val STEM_SYNONYMS =
            mapOf(
                "красн" to
                    listOf(
                        "red",
                        "красн"
                    ),
                "оранж" to
                    listOf(
                        "orange",
                        "оранж"
                    ),
                "желт" to
                    listOf(
                        "yellow",
                        "желт"
                    ),
                "зел" to
                    listOf(
                        "green",
                        "зел"
                    ),
                "голуб" to
                    listOf(
                        "cyan",
                        "blue",
                        "голуб"
                    ),
                "син" to
                    listOf(
                        "blue",
                        "син"
                    ),
                "фиолет" to
                    listOf(
                        "purple",
                        "фиолет"
                    ),
                "розов" to
                    listOf(
                        "pink",
                        "розов"
                    ),
                "корич" to
                    listOf(
                        "brown",
                        "корич"
                    ),
                "черн" to
                    listOf(
                        "black",
                        "черн"
                    ),
                "бел" to
                    listOf(
                        "white",
                        "бел"
                    ),
                "сер" to
                    listOf(
                        "gray",
                        "grey",
                        "сер"
                    ),
                "машин" to
                    listOf(
                        "car",
                        "vehicle",
                        "машин"
                    ),
                "автомоб" to
                    listOf(
                        "car",
                        "vehicle"
                    ),
                "парков" to
                    listOf(
                        "parking",
                        "car",
                        "vehicle"
                    ),
                "книг" to
                    listOf(
                        "book",
                        "isbn",
                        "publisher"
                    ),
                "чек" to
                    listOf(
                        "receipt",
                        "invoice",
                        "total",
                        "итого"
                    ),
                "квитанц" to
                    listOf(
                        "receipt",
                        "invoice"
                    ),
                "билет" to
                    listOf(
                        "ticket",
                        "admission",
                        "event"
                    ),
                "контакт" to
                    listOf(
                        "email",
                        "phone",
                        "tel",
                        "linkedin"
                    ),
                "ключ" to
                    listOf(
                        "key",
                        "keys"
                    ),
                "наушник" to
                    listOf(
                        "headphones",
                        "earphones",
                        "earbuds",
                        "headset"
                    ),
                "стоил" to
                    listOf(
                        "сумма",
                        "цена",
                        "стоимость",
                        "amount",
                        "price",
                        "total",
                        "money"
                    ),
                "цен" to
                    listOf(
                        "сумма",
                        "стоимость",
                        "amount",
                        "price",
                        "total",
                        "money"
                    ),
                "сумм" to
                    listOf(
                        "цена",
                        "стоимость",
                        "amount",
                        "price",
                        "total",
                        "money"
                    ),
                "телефон" to
                    listOf(
                        "phone",
                        "tel",
                        "mobile",
                        "контакт"
                    ),
                "номер" to
                    listOf(
                        "phone",
                        "tel",
                        "mobile",
                        "телефон"
                    ),
                "почт" to
                    listOf(
                        "email",
                        "e-mail",
                        "контакт"
                    ),
                "ссыл" to
                    listOf(
                        "url",
                        "website",
                        "web",
                        "сайт"
                    ),
                "сайт" to
                    listOf(
                        "url",
                        "website",
                        "web",
                        "ссылка"
                    ),
                "дат" to
                    listOf(
                        "date",
                        "дата",
                        "когда"
                    )
            )

        val SYNONYMS = mapOf(
            "чек" to listOf(
                "receipt",
                "invoice",
                "total",
                "итого"
            ),
            "квитанция" to listOf(
                "receipt",
                "invoice",
                "чек"
            ),
            "машина" to listOf(
                "car",
                "vehicle",
                "авто"
            ),
            "авто" to listOf(
                "car",
                "vehicle",
                "машина"
            ),
            "парковка" to listOf(
                "parking",
                "car",
                "vehicle"
            ),
            "парковался" to listOf(
                "парковка",
                "parking",
                "car",
                "vehicle"
            ),
            "парковала" to listOf(
                "парковка",
                "parking",
                "car",
                "vehicle"
            ),
            "книга" to listOf(
                "book",
                "isbn",
                "publisher"
            ),
            "билет" to listOf(
                "ticket",
                "admission",
                "event"
            ),
            "контакт" to listOf(
                "email",
                "phone",
                "tel",
                "linkedin"
            ),
            "ключ" to listOf(
                "key",
                "keys"
            ),
            "ключи" to listOf(
                "key",
                "keys"
            ),
            "наушники" to listOf(
                "headphones",
                "earphones",
                "earbuds",
                "headset"
            )
        )
    }
}
