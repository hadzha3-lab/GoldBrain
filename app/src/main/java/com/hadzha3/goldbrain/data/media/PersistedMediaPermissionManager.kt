package com.hadzha3.goldbrain.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri

class PersistedMediaPermissionManager(
    private val context: Context
) {
    fun releaseReadGrant(
        uri: Uri
    ) {
        val hasGrant =
            context
                .contentResolver
                .persistedUriPermissions
                .any {
                    it.uri == uri &&
                        it.isReadPermission
                }

        if (!hasGrant) {
            return
        }

        runCatching {
            context
                .contentResolver
                .releasePersistableUriPermission(
                    uri,
                    Intent
                        .FLAG_GRANT_READ_URI_PERMISSION
                )
        }
    }

    fun releaseAllReadGrants() {
        context
            .contentResolver
            .persistedUriPermissions
            .filter {
                it.isReadPermission
            }
            .forEach {
                permission ->
                releaseReadGrant(
                    permission.uri
                )
            }
    }
}
