package com.hadzha3.goldbrain.data.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorClassifierTest {
    private val classifier =
        ColorClassifier()

    @Test
    fun redSamplesAreDetectedAsRed() {
        val samples =
            List(100) {
                ColorSample(
                    hue = 2f,
                    saturation = 0.9f,
                    value = 0.8f
                )
            }

        val result =
            classifier.classify(
                samples
            )

        assertEquals(
            "Red",
            result?.english
        )
    }

    @Test
    fun whiteSamplesAreDetectedAsWhite() {
        val samples =
            List(100) {
                ColorSample(
                    hue = 0f,
                    saturation = 0.03f,
                    value = 0.95f
                )
            }

        val result =
            classifier.classify(
                samples
            )

        assertEquals(
            "White",
            result?.english
        )
    }

    @Test
    fun secondaryChromaticColorIsKeptWhenMeaningful() {
        val samples =
            buildList {
                repeat(60) {
                    add(
                        ColorSample(
                            hue = 220f,
                            saturation = 0.8f,
                            value = 0.8f
                        )
                    )
                }

                repeat(25) {
                    add(
                        ColorSample(
                            hue = 2f,
                            saturation = 0.9f,
                            value = 0.8f
                        )
                    )
                }

                repeat(15) {
                    add(
                        ColorSample(
                            hue = 0f,
                            saturation = 0.03f,
                            value = 0.9f
                        )
                    )
                }
            }

        val result =
            classifier.classifyTop(
                samples,
                maxColors = 2
            )

        assertEquals(
            listOf(
                "Blue",
                "Red"
            ),
            result.map {
                it.english
            }
        )
    }

    @Test
    fun tinyAccentColorIsIgnored() {
        val samples =
            buildList {
                repeat(95) {
                    add(
                        ColorSample(
                            hue = 0f,
                            saturation = 0.02f,
                            value = 0.9f
                        )
                    )
                }

                repeat(5) {
                    add(
                        ColorSample(
                            hue = 2f,
                            saturation = 0.9f,
                            value = 0.8f
                        )
                    )
                }
            }

        val result =
            classifier.classifyTop(
                samples
            )

        assertTrue(
            result.none {
                it.english ==
                    "Red"
            }
        )

        assertEquals(
            "White",
            result.first().english
        )
    }
}
