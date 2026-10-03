package com.hadzha3.goldbrain.data.media

import android.net.Uri

interface MediaAccessChecker {
    fun isAvailable(uri: Uri): Boolean
}
