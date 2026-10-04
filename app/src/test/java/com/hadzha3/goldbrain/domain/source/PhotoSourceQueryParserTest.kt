package com.hadzha3.goldbrain.domain.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoSourceQueryParserTest {
    @Test
    fun downloadedQueryFiltersDownloads() {
        assertEquals(
            setOf(
                PhotoSourceType.DOWNLOAD
            ),
            PhotoSourceQueryParser
                .filterFor(
                    "покажи скачанные фото наушников"
                )
        )
    }

    @Test
    fun myPhotosIncludesCameraAndGoldBrain() {
        assertEquals(
            setOf(
                PhotoSourceType.CAMERA,
                PhotoSourceType.GOLDBRAIN
            ),
            PhotoSourceQueryParser
                .filterFor(
                    "найди мои фото машины"
                )
        )
    }

    @Test
    fun messengerQueryIsRecognized() {
        assertEquals(
            setOf(
                PhotoSourceType.MESSENGER
            ),
            PhotoSourceQueryParser
                .filterFor(
                    "фото из telegram"
                )
        )
    }

    @Test
    fun ordinaryQueryDoesNotForceSource() {
        assertEquals(
            null,
            PhotoSourceQueryParser
                .filterFor(
                    "синяя машина"
                )
        )
    }

    @Test
    fun sourceWordsAreRemovedFromContentTerms() {
        assertTrue(
            PhotoSourceQueryParser
                .isSourceIntentWord(
                    "скачанные"
                )
        )

        assertTrue(
            PhotoSourceQueryParser
                .isSourceIntentWord(
                    "мои"
                )
        )
    }
}
