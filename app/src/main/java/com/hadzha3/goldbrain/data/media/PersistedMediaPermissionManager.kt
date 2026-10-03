package com.hadzha3.goldbrain.data.media

import android.content.Context
import android.content.Intent

class PersistedMediaPermissionManager(
    private val context: Context
) {
    fun releaseAllReadGrants() {
        context
            .contentResolver
            .persistedUriPermissions
            .filter {
                it.isReadPermission
            }
            .forEach { permission ->
                runCatching {
                    context
                        .contentResolver
                        .releasePersistableUriPermission(
                            permission.uri,
                            Intent
                                .FLAG_GRANT_READ_URI_PERMISSION
                        )
                }
            }
    }
}
