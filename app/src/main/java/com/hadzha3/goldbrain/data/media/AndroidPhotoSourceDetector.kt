package com.hadzha3.goldbrain.data.media

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.hadzha3.goldbrain.domain.source.PhotoSourceClassification
import com.hadzha3.goldbrain.domain.source.PhotoSourceClassifier
import com.hadzha3.goldbrain.domain.source.PhotoSourceMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPhotoSourceDetector(
    private val context: Context,
    private val classifier:
        PhotoSourceClassifier
) : PhotoSourceDetector {
    override suspend fun detect(
        uri: Uri
    ): PhotoSourceClassification =
        withContext(
            Dispatchers.IO
        ) {
            val metadata =
                if (
                    uri.authority ==
                    MediaStore.AUTHORITY
                ) {
                    queryMediaStore(
                        uri
                    )
                } else {
                    queryGeneric(
                        uri
                    )
                }

            classifier.classify(
                metadata.copy(
                    authority =
                        uri.authority
                )
            )
        }

    private fun queryMediaStore(
        uri: Uri
    ): PhotoSourceMetadata {
        val projection =
            buildList {
                add(
                    MediaStore.MediaColumns
                        .DISPLAY_NAME
                )

                add(
                    MediaStore.MediaColumns
                        .BUCKET_DISPLAY_NAME
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
                            .OWNER_PACKAGE_NAME
                    )
                }
            }.toTypedArray()

        return runCatching {
            context.contentResolver
                .query(
                    uri,
                    projection,
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    if (
                        !cursor.moveToFirst()
                    ) {
                        return@use
                            PhotoSourceMetadata()
                    }

                    PhotoSourceMetadata(
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
                            cursor.stringOrNull(
                                MediaStore.MediaColumns
                                    .BUCKET_DISPLAY_NAME
                            ),
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
                }
        }.getOrNull()
            ?: queryGeneric(
                uri
            )
    }

    private fun queryGeneric(
        uri: Uri
    ): PhotoSourceMetadata =
        runCatching {
            context.contentResolver
                .query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    if (
                        !cursor.moveToFirst()
                    ) {
                        return@use
                            PhotoSourceMetadata()
                    }

                    PhotoSourceMetadata(
                        displayName =
                            cursor.stringOrNull(
                                OpenableColumns
                                    .DISPLAY_NAME
                            )
                    )
                }
        }.getOrNull()
            ?: PhotoSourceMetadata(
                authority =
                    uri.authority
            )

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
