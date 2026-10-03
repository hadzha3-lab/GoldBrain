package com.hadzha3.goldbrain.data.gallery

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaStoreGalleryMediaSource(
    private val context: Context
) : GalleryMediaSource {
    override fun unindexedImages(
        indexedUris: Set<String>,
        limit: Int
    ): List<GalleryMediaItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )

        val result = ArrayList<GalleryMediaItem>(limit)

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

                result += GalleryMediaItem(
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
}
