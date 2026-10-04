package com.hadzha3.goldbrain.data.media

import android.content.Context
import android.os.Build
import android.provider.MediaStore

class MediaStoreChangeTracker(
    private val context: Context
) {
    private val preferences =
        context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )

    fun shouldSkipAutomaticScan():
        Boolean {
        val current =
            currentSignature()
                ?: return false

        val lastSynced =
            preferences.getString(
                KEY_LAST_SYNCED,
                null
            )

        return current ==
            lastSynced
    }

    fun beginScan() {
        val signature =
            currentSignature()

        preferences.edit()
            .apply {
                if (
                    signature == null
                ) {
                    remove(
                        KEY_PENDING
                    )
                } else {
                    putString(
                        KEY_PENDING,
                        signature
                    )
                }
            }
            .apply()
    }

    fun markScanComplete() {
        val pending =
            preferences.getString(
                KEY_PENDING,
                null
            )

        preferences.edit()
            .apply {
                if (
                    pending != null
                ) {
                    putString(
                        KEY_LAST_SYNCED,
                        pending
                    )
                }

                remove(
                    KEY_PENDING
                )
            }
            .apply()
    }

    fun invalidate() {
        preferences.edit()
            .remove(
                KEY_LAST_SYNCED
            )
            .remove(
                KEY_PENDING
            )
            .apply()
    }

    private fun currentSignature():
        String? {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.R
        ) {
            return null
        }

        return runCatching {
            MediaStore
                .getExternalVolumeNames(
                    context
                )
                .sorted()
                .joinToString(
                    separator = "|"
                ) { volume ->
                    val version =
                        MediaStore.getVersion(
                            context,
                            volume
                        )

                    val generation =
                        MediaStore.getGeneration(
                            context,
                            volume
                        )

                    "$volume:$version:$generation"
                }
        }.getOrNull()
    }

    private companion object {
        const val FILE_NAME =
            "goldbrain_media_sync"

        const val KEY_LAST_SYNCED =
            "last_synced_signature"

        const val KEY_PENDING =
            "pending_signature"
    }
}
