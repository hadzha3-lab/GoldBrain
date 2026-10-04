package com.hadzha3.goldbrain.data.gallery

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.hadzha3.goldbrain.domain.source.PhotoSourceClassifier
import com.hadzha3.goldbrain.domain.source.PhotoSourceMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreGalleryMediaSource(
    private val context: Context,
    private val sourceClassifier:
        PhotoSourceClassifier
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
                buildList {
                    add(
                        MediaStore.Images.Media._ID
                    )
                    add(
                        MediaStore.Images.Media.DATE_TAKEN
                    )
                    add(
                        MediaStore.Images.Media.DATE_ADDED
                    )
                    add(
                        MediaStore.MediaColumns
                            .DISPLAY_NAME
                    )

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.Q
                    ) {
                        add(
                            MediaStore.MediaColumns
                                .RELATIVE_PATH
                        )
                        add(
                            MediaStore.MediaColumns
                                .BUCKET_DISPLAY_NAME
                        )
                        add(
                            MediaStore.MediaColumns
                                .OWNER_PACKAGE_NAME
                        )
                    }
                }.toTypedArray()

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

                    val source =
                        sourceClassifier
                            .classify(
                                PhotoSourceMetadata(
                                    authority =
                                        uri.authority,
                                    displayName =
                                        cursor.stringOrNull(
                                            MediaStore.MediaColumns
                                                .DISPLAY_NAME
                                        ),
                                    relativePath =
                                        if (
                                            Build.VERSION.SDK_INT >=
                                            Build.VERSION_CODES.Q
                                        ) {
                                            cursor.stringOrNull(
                                                MediaStore.MediaColumns
                                                    .RELATIVE_PATH
                                            )
                                        } else {
                                            null
                                        },
                                    bucketDisplayName =
                                        if (
                                            Build.VERSION.SDK_INT >=
                                            Build.VERSION_CODES.Q
                                        ) {
                                            cursor.stringOrNull(
                                                MediaStore.MediaColumns
                                                    .BUCKET_DISPLAY_NAME
                                            )
                                        } else {
                                            null
                                        },
                                    ownerPackageName =
                                        if (
                                            Build.VERSION.SDK_INT >=
                                            Build.VERSION_CODES.Q
                                        ) {
                                            cursor.stringOrNull(
                                                MediaStore.MediaColumns
                                                    .OWNER_PACKAGE_NAME
                                            )
                                        } else {
                                            null
                                        }
                                )
                            )

                    page +=
                        GalleryMediaItem(
                            uri = uri,
                            createdAt =
                                createdAt,
                            source =
                                source
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

    private fun android.database.Cursor
        .stringOrNull(
            column: String
        ): String? {
        val index =
            getColumnIndex(
                column
            )

        return if (
            index >= 0 &&
            !isNull(index)
        ) {
            getString(index)
        } else {
            null
        }
    }
}
