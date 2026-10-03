package com.hadzha3.goldbrain.data.media

import android.content.Context
import android.net.Uri

class AndroidMediaAccessChecker(
    private val context: Context
) : MediaAccessChecker {
    override fun isAvailable(
        uri: Uri
    ): Boolean =
        runCatching {
            context.contentResolver
                .openFileDescriptor(uri, "r")
                ?.use { true }
                ?: false
        }.getOrDefault(false)
}
