package com.hadzha3.goldbrain.data.analysis

import android.net.Uri

interface ImageAnalyzer {
    suspend fun analyze(uri: Uri): ImageAnalysis
}
