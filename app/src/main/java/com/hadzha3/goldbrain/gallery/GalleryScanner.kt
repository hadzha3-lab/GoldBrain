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
        val failed: Int,
        val candidates: Int,
        val hasMore: Boolean
    )

    suspend fun scanNextBatch(
        batchSize: Int = 30
    ): ScanResult {
        val repository = MemoryRepository(context)
        val indexedUris = repository.indexedUris()
        val candidates = queryUnindexed(
            indexedUris = indexedUris,
            limit = batchSize
        )

        var indexed = 0
        var failed = 0

        candidates.forEach { item ->
            val result = runCatching {
                repository.index(
                    uri = item.uri,
                    createdAt = item.createdAt
                )
            }
            if (result.isSuccess) indexed++ else failed++
        }

        return ScanResult(
            indexed = indexed,
            failed = failed,
            candidates = candidates.size,
            hasMore = candidates.size >= batchSize
        )
    }

    private fun queryUnindexed(
        indexedUris: Set<String>,
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

            while (
                cursor.moveToNext() &&
                result.size < limit
            ) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                if (uri.toString() in indexedUris) {
                    continue
                }

                val dateTaken = cursor.getLong(takenColumn)
                val dateAddedSeconds = cursor.getLong(addedColumn)

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
