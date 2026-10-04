package com.hadzha3.goldbrain.data.analysis

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplementalOcrHeuristicsTest {
    @Test
    fun tinyIncidentalTextSkipsCyrillicPass() {
        assertFalse(
            SupplementalOcrHeuristics
                .shouldRunCyrillicOcr(
                    primaryText =
                        "123",
                    labels =
                        emptyList()
                )
        )
    }

    @Test
    fun documentLabelEnablesCyrillicPass() {
        assertTrue(
            SupplementalOcrHeuristics
                .shouldRunCyrillicOcr(
                    primaryText = "",
                    labels =
                        listOf(
                            "Document"
                        )
                )
        )
    }

    @Test
    fun ordinaryObjectWithoutTextSkipsSlowOcr() {
        assertFalse(
            SupplementalOcrHeuristics
                .shouldRunCyrillicOcr(
                    primaryText = "",
                    labels =
                        listOf(
                            "Car",
                            "Road",
                            "Sky"
                        )
                )
        )
    }

    @Test
    fun screenshotEnablesCyrillicPass() {
        assertTrue(
            SupplementalOcrHeuristics
                .shouldRunCyrillicOcr(
                    primaryText = "",
                    labels =
                        listOf(
                            "Screenshot"
                        )
                )
        )
    }

    @Test
    fun substantialPrimaryTextEnablesFallbackWithoutLabel() {
        assertTrue(
            SupplementalOcrHeuristics
                .shouldRunCyrillicOcr(
                    primaryText =
                        "Order number 123456, delivery address and additional details",
                    labels =
                        listOf(
                            "Indoor"
                        )
                )
        )
    }

    @Test
    fun alreadyUsefulCyrillicTextSkipsDuplicatePass() {
        assertFalse(
            SupplementalOcrHeuristics
                .shouldRunCyrillicOcr(
                    primaryText =
                        "Это уже достаточно длинный русский текст, распознанный первым OCR без дополнительного прохода",
                    labels =
                        listOf(
                            "Document"
                        )
                )
        )
    }
}
