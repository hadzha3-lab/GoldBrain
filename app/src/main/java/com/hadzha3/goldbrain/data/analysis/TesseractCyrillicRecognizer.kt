package com.hadzha3.goldbrain.data.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

class TesseractCyrillicRecognizer(
    private val context: Context
) {
    private val mutex =
        Mutex()

    private var tessBaseApi:
        TessBaseAPI? =
        null

    suspend fun recognize(
        uri: Uri
    ): String =
        withContext(
            Dispatchers.IO
        ) {
            mutex.withLock {
                val tess =
                    getOrCreateApi()
                        ?: return@withLock ""

                val bitmap =
                    decodeForOcr(
                        uri
                    ) ?: return@withLock ""

                try {
                    tess.setImage(
                        bitmap
                    )

                    tess.getUTF8Text()
                        .orEmpty()
                        .trim()
                } finally {
                    bitmap.recycle()
                }
            }
        }

    private fun getOrCreateApi():
        TessBaseAPI? {
        tessBaseApi
            ?.let {
                return it
            }

        if (
            !ensureModel()
        ) {
            return null
        }

        val root =
            File(
                context.filesDir,
                DATA_ROOT
            )

        val candidate =
            TessBaseAPI()

        val initialized =
            runCatching {
                candidate.init(
                    root.absolutePath,
                    LANGUAGE
                )
            }.getOrDefault(
                false
            )

        if (
            !initialized
        ) {
            candidate.recycle()
            return null
        }

        candidate.setPageSegMode(
            TessBaseAPI.PageSegMode.PSM_AUTO
        )

        tessBaseApi =
            candidate

        return candidate
    }

    private fun ensureModel():
        Boolean {
        val root =
            File(
                context.filesDir,
                DATA_ROOT
            )

        val tessData =
            File(
                root,
                TESSDATA_DIRECTORY
            )

        val model =
            File(
                tessData,
                MODEL_FILE
            )

        if (
            model.exists() &&
            model.length() > 0L
        ) {
            return true
        }

        if (
            !tessData.exists() &&
            !tessData.mkdirs()
        ) {
            return false
        }

        val temporary =
            File(
                tessData,
                "$MODEL_FILE.tmp"
            )

        return runCatching {
            context.assets
                .open(
                    ASSET_PATH
                )
                .use { input ->
                    temporary
                        .outputStream()
                        .buffered()
                        .use { output ->
                            input.copyTo(
                                output
                            )
                        }
                }

            if (
                temporary.length() <=
                0L
            ) {
                temporary.delete()
                return@runCatching false
            }

            if (
                model.exists()
            ) {
                model.delete()
            }

            val renamed =
                temporary.renameTo(
                    model
                )

            if (
                !renamed
            ) {
                temporary
                    .copyTo(
                        target = model,
                        overwrite = true
                    )

                temporary.delete()
            }

            model.exists() &&
                model.length() > 0L
        }.getOrElse {
            temporary.delete()
            false
        }
    }

    private fun decodeForOcr(
        uri: Uri
    ): Bitmap? {
        val orientation =
            readOrientation(
                uri
            )

        val bounds =
            BitmapFactory.Options()
                .apply {
                    inJustDecodeBounds =
                        true
                }

        context
            .contentResolver
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
                        Bitmap.Config.ARGB_8888
                }

        val decoded =
            context
                .contentResolver
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
                ?: return null

        return applyOrientation(
            bitmap = decoded,
            orientation = orientation
        )
    }

    private fun readOrientation(
        uri: Uri
    ): Int =
        runCatching {
            context
                .contentResolver
                .openInputStream(
                    uri
                )
                ?.use { input ->
                    ExifInterface(
                        input
                    ).getAttributeInt(
                        ExifInterface
                            .TAG_ORIENTATION,
                        ExifInterface
                            .ORIENTATION_NORMAL
                    )
                }
                ?: ExifInterface
                    .ORIENTATION_NORMAL
        }.getOrDefault(
            ExifInterface
                .ORIENTATION_NORMAL
        )

    private fun applyOrientation(
        bitmap: Bitmap,
        orientation: Int
    ): Bitmap {
        val matrix =
            Matrix()

        when (orientation) {
            ExifInterface
                .ORIENTATION_FLIP_HORIZONTAL ->
                matrix.setScale(
                    -1f,
                    1f
                )

            ExifInterface
                .ORIENTATION_ROTATE_180 ->
                matrix.setRotate(
                    180f
                )

            ExifInterface
                .ORIENTATION_FLIP_VERTICAL ->
                matrix.setScale(
                    1f,
                    -1f
                )

            ExifInterface
                .ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(
                    90f
                )
                matrix.postScale(
                    -1f,
                    1f
                )
            }

            ExifInterface
                .ORIENTATION_ROTATE_90 ->
                matrix.setRotate(
                    90f
                )

            ExifInterface
                .ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(
                    -90f
                )
                matrix.postScale(
                    -1f,
                    1f
                )
            }

            ExifInterface
                .ORIENTATION_ROTATE_270 ->
                matrix.setRotate(
                    -90f
                )

            else ->
                return bitmap
        }

        val transformed =
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

        if (
            transformed !==
            bitmap
        ) {
            bitmap.recycle()
        }

        return transformed
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int
    ): Int {
        var sample =
            1

        while (
            max(
                width / sample,
                height / sample
            ) >
            MAX_OCR_DIMENSION
        ) {
            sample *=
                2
        }

        return sample
            .coerceAtLeast(
                1
            )
    }

    private companion object {
        const val DATA_ROOT =
            "tesseract"

        const val TESSDATA_DIRECTORY =
            "tessdata"

        const val MODEL_FILE =
            "rus.traineddata"

        const val ASSET_PATH =
            "tessdata/rus.traineddata"

        const val LANGUAGE =
            "rus"

        const val MAX_OCR_DIMENSION =
            2200
    }
}
