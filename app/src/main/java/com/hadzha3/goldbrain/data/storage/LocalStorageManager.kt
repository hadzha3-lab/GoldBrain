package com.hadzha3.goldbrain.data.storage

import android.content.Context
import com.hadzha3.goldbrain.data.local.AppDatabase
import java.io.File

data class LocalStorageUsage(
    val indexBytes: Long,
    val cacheBytes: Long
)

class LocalStorageManager(
    private val context: Context
) {
    fun usage(): LocalStorageUsage {
        val database =
            context.getDatabasePath(
                AppDatabase.DATABASE_NAME
            )

        val indexBytes =
            listOf(
                database,
                File(
                    database.path +
                        "-wal"
                ),
                File(
                    database.path +
                        "-shm"
                ),
                File(
                    database.path +
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
