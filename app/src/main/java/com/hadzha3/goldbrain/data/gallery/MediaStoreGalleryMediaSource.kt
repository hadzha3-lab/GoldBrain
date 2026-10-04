package com.hadzha3.goldbrain.data.gallery

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreGalleryMediaSource(
    private val context: Context
) : GalleryMediaSource {
    override suspend fun scanImages(
        startOffset: Int,
        pageSize: Int,
        onPage:
            suspend (
                List<GalleryMediaItem>
            ) -> Boolean
    ): GalleryScanResult =
        withContext(
            Dispatchers.IO
        ) {
            val projection =
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED
                )

            context.contentResolver.query(
                MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC, " +
                    "${MediaStore.Images.Media._ID} DESC"
            )?.use { cursor ->
                if (
                    startOffset > 0 &&
                    !cursor.moveToPosition(
                        startOffset - 1
                    )
                ) {
                    return@withContext GalleryScanResult(
                            nextOffset =
                                startOffset,
                            reachedEnd =
                                true
                        )
                }

                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID
                    )

                val takenColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DATE_TAKEN
                    )

                val addedColumn =
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DATE_ADDED
                    )

                var nextOffset =
                    startOffset

                val page =
                    ArrayList<GalleryMediaItem>(
                        pageSize
                    )

                while (
                    cursor.moveToNext()
                ) {
                    val id =
                        cursor.getLong(
                            idColumn
                        )

                    val uri =
                        ContentUris
                            .withAppendedId(
                                MediaStore.Images.Media
                                    .EXTERNAL_CONTENT_URI,
                                id
                            )

                    val dateTaken =
                        cursor.getLong(
                            takenColumn
                        )

                    val dateAddedSeconds =
                        cursor.getLong(
                            addedColumn
                        )

                    val createdAt =
                        when {
                            dateTaken > 0L ->
                                dateTaken

                            dateAddedSeconds > 0L ->
                                dateAddedSeconds *
                                    1000L

                            else ->
                                System.currentTimeMillis()
                        }

                    page +=
                        GalleryMediaItem(
                            uri = uri,
                            createdAt =
                                createdAt
                        )

                    nextOffset++

                    if (
                        page.size >=
                        pageSize
                    ) {
                        val shouldContinue =
                            onPage(
                                page.toList()
                            )

                        page.clear()

                        if (!shouldContinue) {
                            return@withContext GalleryScanResult(
                                    nextOffset =
                                        nextOffset,
                                    reachedEnd =
                                        cursor.isLast
                                )
                        }
                    }
                }

                if (
                    page.isNotEmpty()
                ) {
                    onPage(
                        page.toList()
                    )
                }

                GalleryScanResult(
                    nextOffset =
                        nextOffset,
                    reachedEnd =
                        true
                )
            }
                ?: GalleryScanResult(
                    nextOffset =
                        startOffset,
                    reachedEnd =
                        true
                )
        }
}
