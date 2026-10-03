package com.hadzha3.goldbrain.data.analysis

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplementalOcrHeuristicsTest {
    @Test
    fun textDetectedByPrimaryOcrEnablesCyrillicPass() {
        assertTrue(
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
}
