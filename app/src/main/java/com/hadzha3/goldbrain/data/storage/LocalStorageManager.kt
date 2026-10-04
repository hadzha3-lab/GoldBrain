package com.hadzha3.goldbrain.data.storage

import android.content.Context
import com.hadzha3.goldbrain.data.local.AppDatabase
import java.io.File

data class LocalStorageUsage(
    val indexBytes: Long,
    val cacheBytes: Long
)

class LocalStorageManager(
    private val context: Context,
    private val database: AppDatabase
) {
    fun usage(): LocalStorageUsage {
        val databaseFile =
            context.getDatabasePath(
                AppDatabase.DATABASE_NAME
            )

        val indexBytes =
            listOf(
                databaseFile,
                File(
                    databaseFile.path +
                        "-wal"
                ),
                File(
                    databaseFile.path +
                        "-shm"
                ),
                File(
                    databaseFile.path +
                        "-journal"
                )
            ).sumOf(
                ::safeLength
            )

        return LocalStorageUsage(
            indexBytes =
                indexBytes,
            cacheBytes =
                directoryBytes(
                    context.cacheDir
                )
        )
    }

    fun optimizeIndex() {
        database
            .openHelper
            .writableDatabase
            .execSQL(
                "VACUUM"
            )
    }

    fun clearTemporaryCache() {
        context.cacheDir
            .listFiles()
            ?.forEach { file ->
                runCatching {
                    file.deleteRecursively()
                }
            }
    }

    private fun directoryBytes(
        directory: File
    ): Long =
        runCatching {
            directory
                .walkTopDown()
                .filter {
                    it.isFile
                }
                .sumOf {
                    safeLength(it)
                }
        }.getOrDefault(
            0L
        )

    private fun safeLength(
        file: File
    ): Long =
        runCatching {
            if (file.exists()) {
                file.length()
            } else {
                0L
            }
        }.getOrDefault(
            0L
        )
}
