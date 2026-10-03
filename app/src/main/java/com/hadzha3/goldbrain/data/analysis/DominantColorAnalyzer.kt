package com.hadzha3.goldbrain.data.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class DominantColorAnalyzer(
    private val context: Context,
    private val classifier:
        ColorClassifier =
        ColorClassifier()
) {
    suspend fun analyze(
        uri: Uri
    ): List<DetectedColor> =
        withContext(
            Dispatchers.IO
        ) {
            val bitmap =
                decodePreview(
                    uri
                ) ?: return@withContext emptyList()

            try {
                classifier.classifyTop(
                    samples = sampleBitmap(
                        bitmap
                    ),
                    maxColors = MAX_COLOR_LABELS
                )
            } finally {
                bitmap.recycle()
            }
        }

    private fun decodePreview(
        uri: Uri
    ): Bitmap? {
        val resolver =
            context.contentResolver

        val bounds =
            BitmapFactory.Options()
                .apply {
                    inJustDecodeBounds =
                        true
                }

        resolver
            .openInputStream(
                uri
            )
            ?.use { input ->
                BitmapFactory
                    .decodeStream(
                        input,
                        null,
                        bounds
                    )
            }

        if (
            bounds.outWidth <= 0 ||
            bounds.outHeight <= 0
        ) {
            return null
        }

        val options =
            BitmapFactory.Options()
                .apply {
                    inSampleSize =
                        calculateSampleSize(
                            width =
                                bounds.outWidth,
                            height =
                                bounds.outHeight
                        )

                    inPreferredConfig =
                        Bitmap.Config.RGB_565
                }

        return resolver
            .openInputStream(
                uri
            )
            ?.use { input ->
                BitmapFactory
                    .decodeStream(
                        input,
                        null,
                        options
                    )
            }
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int
    ): Int {
        var sample = 1

        while (
            max(
                width / sample,
                height / sample
            ) >
            TARGET_MAX_DIMENSION
        ) {
            sample *= 2
        }

        return sample
            .coerceAtLeast(1)
    }

    private fun sampleBitmap(
        bitmap: Bitmap
    ): List<ColorSample> {
        val width =
            bitmap.width

        val height =
            bitmap.height

        if (
            width <= 0 ||
            height <= 0
        ) {
            return emptyList()
        }

        val startX =
            (
                width *
                    CROP_START
            ).toInt()

        val endX =
            (
                width *
                    CROP_END
            ).toInt()
                .coerceAtLeast(
                    startX + 1
                )

        val startY =
            (
                height *
                    CROP_START
            ).toInt()

        val endY =
            (
                height *
                    CROP_END
            ).toInt()
                .coerceAtLeast(
                    startY + 1
                )

        val croppedWidth =
            endX - startX

        val croppedHeight =
            endY - startY

        val step =
            max(
                1,
                max(
                    croppedWidth,
                    croppedHeight
                ) /
                    SAMPLE_GRID
            )

        val hsv =
            FloatArray(3)

        val samples =
            ArrayList<ColorSample>()

        var y =
            startY

        while (y < endY) {
            var x =
                startX

            while (x < endX) {
                val pixel =
                    bitmap.getPixel(
                        x,
                        y
                    )

                Color.colorToHSV(
                    pixel,
                    hsv
                )

                samples +=
                    ColorSample(
                        hue =
                            hsv[0],
                        saturation =
                            hsv[1],
                        value =
                            hsv[2]
                    )

                x += step
            }

            y += step
        }

        return samples
    }

    private companion object {
        const val TARGET_MAX_DIMENSION =
            192

        const val MAX_COLOR_LABELS =
            2

        const val SAMPLE_GRID =
            48

        const val CROP_START =
            0.08f

        const val CROP_END =
            0.92f
    }
}
