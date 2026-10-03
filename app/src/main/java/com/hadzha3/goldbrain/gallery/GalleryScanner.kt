package com.hadzha3.goldbrain.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.hadzha3.goldbrain.data.MemoryRepository

class GalleryScanner(
    private val context: Context
) {
    data class ScanResult(
        val indexed: Int,
        val skipped: Int,
        val failed: Int,
        val hasMore: Boolean
    )

    suspend fun scanRecent(
        batchSize: Int = 40
    ): ScanResult {
        val repository = MemoryRepository(context)
        val items = queryImages(limit = batchSize + 1)

        var indexed = 0
        var skipped = 0
        var failed = 0

        items.take(batchSize).forEach { item ->
            if (repository.exists(item.uri)) {
                skipped++
            } else {
                val result = runCatching {
                    repository.index(
                        uri = item.uri,
                        createdAt = item.createdAt
                    )
                }
                if (result.isSuccess) indexed++ else failed++
            }
        }

        return ScanResult(
            indexed = indexed,
            skipped = skipped,
            failed = failed,
            hasMore = items.size > batchSize
        )
    }

    private fun queryImages(
        limit: Int
    ): List<GalleryItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )

        val result = mutableListOf<GalleryItem>()

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID
            )
            val takenColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATE_TAKEN
            )
            val addedColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATE_ADDED
            )

            while (cursor.moveToNext() && result.size < limit) {
                val id = cursor.getLong(idColumn)
                val dateTaken = cursor.getLong(takenColumn)
                val dateAddedSeconds = cursor.getLong(addedColumn)

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                result += GalleryItem(
                    uri = uri,
                    createdAt = when {
                        dateTaken > 0L -> dateTaken
                        dateAddedSeconds > 0L -> dateAddedSeconds * 1000L
                        else -> System.currentTimeMillis()
                    }
                )
            }
        }

        return result
    }

    private data class GalleryItem(
        val uri: Uri,
        val createdAt: Long
    )
}
