package com.hadzha3.goldbrain.data.analysis

data class ColorSample(
    val hue: Float,
    val saturation: Float,
    val value: Float
)

data class DetectedColor(
    val english: String,
    val russian: String
) {
    fun labels(): List<String> =
        listOf(
            english,
            russian
        )
}

class ColorClassifier {
    fun classify(
        samples: List<ColorSample>
    ): DetectedColor? {
        if (samples.isEmpty()) {
            return null
        }

        val buckets =
            linkedMapOf<String, Int>()

        samples.forEach { sample ->
            val key =
                classifySample(
                    sample
                )

            buckets[key] =
                buckets.getOrDefault(
                    key,
                    0
                ) + 1
        }

        val total =
            samples.size
                .coerceAtLeast(1)

        val chromatic =
            buckets
                .filterKeys {
                    it !in
                        NEUTRAL_COLORS
                }
                .maxByOrNull {
                    it.value
                }

        if (
            chromatic != null &&
            chromatic.value
                .toFloat() /
                total.toFloat() >=
            MIN_CHROMATIC_RATIO
        ) {
            return COLORS[
                chromatic.key
            ]
        }

        val neutral =
            buckets
                .filterKeys {
                    it in
                        NEUTRAL_COLORS
                }
                .maxByOrNull {
                    it.value
                }

        val winner =
            neutral
                ?: buckets
                    .maxByOrNull {
                        it.value
                    }
                ?: return null

        return COLORS[
            winner.key
        ]
    }

    private fun classifySample(
        sample: ColorSample
    ): String {
        val hue =
            sample.hue
                .coerceIn(
                    0f,
                    360f
                )

        val saturation =
            sample.saturation
                .coerceIn(
                    0f,
                    1f
                )

        val value =
            sample.value
                .coerceIn(
                    0f,
                    1f
                )

        if (
            value <=
            BLACK_VALUE_MAX
        ) {
            return BLACK
        }

        if (
            saturation <=
            NEUTRAL_SATURATION_MAX
        ) {
            return when {
                value >=
                    WHITE_VALUE_MIN ->
                    WHITE

                else ->
                    GRAY
            }
        }

        if (
            hue in BROWN_HUE_MIN..
                BROWN_HUE_MAX &&
            value <=
                BROWN_VALUE_MAX
        ) {
            return BROWN
        }

        return when {
            hue < 15f ||
                hue >= 345f ->
                RED

            hue < 42f ->
                ORANGE

            hue < 70f ->
                YELLOW

            hue < 165f ->
                GREEN

            hue < 195f ->
                CYAN

            hue < 255f ->
                BLUE

            hue < 290f ->
                PURPLE

            hue < 345f ->
                PINK

            else ->
                RED
        }
    }

    private companion object {
        const val MIN_CHROMATIC_RATIO =
            0.08f

        const val BLACK_VALUE_MAX =
            0.16f

        const val WHITE_VALUE_MIN =
            0.86f

        const val NEUTRAL_SATURATION_MAX =
            0.14f

        const val BROWN_HUE_MIN =
            12f

        const val BROWN_HUE_MAX =
            48f

        const val BROWN_VALUE_MAX =
            0.68f

        const val RED = "red"
        const val ORANGE = "orange"
        const val YELLOW = "yellow"
        const val GREEN = "green"
        const val CYAN = "cyan"
        const val BLUE = "blue"
        const val PURPLE = "purple"
        const val PINK = "pink"
        const val BROWN = "brown"
        const val BLACK = "black"
        const val WHITE = "white"
        const val GRAY = "gray"

        val NEUTRAL_COLORS =
            setOf(
                BLACK,
                WHITE,
                GRAY
            )

        val COLORS =
            mapOf(
                RED to
                    DetectedColor(
                        "Red",
                        "Красный"
                    ),
                ORANGE to
                    DetectedColor(
                        "Orange",
                        "Оранжевый"
                    ),
                YELLOW to
                    DetectedColor(
                        "Yellow",
                        "Жёлтый"
                    ),
                GREEN to
                    DetectedColor(
                        "Green",
                        "Зелёный"
                    ),
                CYAN to
                    DetectedColor(
                        "Cyan",
                        "Голубой"
                    ),
                BLUE to
                    DetectedColor(
                        "Blue",
                        "Синий"
                    ),
                PURPLE to
                    DetectedColor(
                        "Purple",
                        "Фиолетовый"
                    ),
                PINK to
                    DetectedColor(
                        "Pink",
                        "Розовый"
                    ),
                BROWN to
                    DetectedColor(
                        "Brown",
                        "Коричневый"
                    ),
                BLACK to
                    DetectedColor(
                        "Black",
                        "Чёрный"
                    ),
                WHITE to
                    DetectedColor(
                        "White",
                        "Белый"
                    ),
                GRAY to
                    DetectedColor(
                        "Gray",
                        "Серый"
                    )
            )
    }
}
