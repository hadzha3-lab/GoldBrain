package com.hadzha3.goldbrain.data.analysis

data class ImageAnalysis(
    val title: String,
    val category: String,
    val text: String,
    val labels: List<String>
)
