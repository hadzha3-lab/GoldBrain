package com.hadzha3.goldbrain.data.media

import android.net.Uri
import com.hadzha3.goldbrain.domain.source.PhotoSourceClassification

interface PhotoSourceDetector {
    suspend fun detect(
        uri: Uri
    ): PhotoSourceClassification
}
