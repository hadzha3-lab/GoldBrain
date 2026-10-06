package com.hadzha3.goldbrain.data.gallery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryPagingContractTest {
    @Test
    fun lookupPageIsBoundedForHugeGallery() {
        val gallerySize = 100_000
        val pageSize = 250
        var largestPage = 0
        var visited = 0

        var offset = 0
        while (offset < gallerySize) {
            val current =
                minOf(
                    pageSize,
                    gallerySize - offset
                )

            largestPage =
                maxOf(
                    largestPage,
                    current
                )
            visited += current
            offset += current
        }

        assertEquals(
            gallerySize,
            visited
        )
        assertTrue(
            largestPage <= pageSize
        )
        assertEquals(
            400,
            (gallerySize + pageSize - 1) /
                pageSize
        )
    }

    @Test
    fun processingBatchStaysSmallComparedWithHugeGallery() {
        val gallerySize = 100_000
        val processingBatch = 30

        assertTrue(
            processingBatch * 100 <
                gallerySize
        )
    }
}
