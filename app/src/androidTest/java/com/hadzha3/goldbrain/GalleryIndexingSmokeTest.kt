package com.hadzha3.goldbrain

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hadzha3.goldbrain.background.GalleryIndexScheduler
import com.hadzha3.goldbrain.core.permissions.GalleryAccessMode
import com.hadzha3.goldbrain.core.permissions.MediaPermissions
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryIndexingSmokeTest {
    @Test
    fun fullAccessIndexesRealMediaStorePhotoWithBundledModels() =
        runBlocking {
            val instrumentation =
                InstrumentationRegistry
                    .getInstrumentation()

            val context =
                instrumentation.targetContext

            grantGalleryAccess(
                instrumentation = instrumentation,
                context = context
            )

            assertEquals(
                GalleryAccessMode.FULL,
                MediaPermissions.galleryAccessMode(
                    context
                )
            )

            val fixtureUri =
                createTextFixture(
                    context
                )

            // Exercise the bundled ML Kit clients directly so a silently missing
            // first-run model cannot be hidden by the app's best-effort fallbacks.
            val image =
                InputImage.fromFilePath(
                    context,
                    fixtureUri
                )

            val textRecognizer =
                TextRecognition.getClient(
                    TextRecognizerOptions
                        .DEFAULT_OPTIONS
                )

            val labeler =
                ImageLabeling.getClient(
                    ImageLabelerOptions
                        .DEFAULT_OPTIONS
                )

            try {
                val recognizedText =
                    withTimeout(
                        MODEL_TIMEOUT_MS
                    ) {
                        textRecognizer
                            .process(image)
                            .await()
                            .text
                    }

                assertTrue(
                    "Bundled OCR model did not recognize the fixture text: $recognizedText",
                    recognizedText.contains(
                        "GOLDBRAIN",
                        ignoreCase = true
                    ) ||
                        recognizedText.contains(
                            "12345"
                        )
                )

                withTimeout(
                    MODEL_TIMEOUT_MS
                ) {
                    labeler
                        .process(image)
                        .await()
                }
            } finally {
                textRecognizer.close()
                labeler.close()
            }

            val container =
                context.appContainer

            container.galleryIndexer.abortScan()

            // Use the same WorkManager path the home screen uses after the user
            // grants gallery access. This catches scheduler/worker regressions that
            // a direct GalleryIndexer call would miss.
            GalleryIndexScheduler.startNow(
                context = context,
                forceScan = true
            )

            val indexedMemory =
                withTimeout(
                    INDEX_TIMEOUT_MS
                ) {
                    container.memoryRepository
                        .memory(
                            fixtureUri.toString()
                        )
                        .filterNotNull()
                        .first()
                }

            assertNotNull(
                indexedMemory
            )

            assertTrue(
                "Indexed fixture has no searchable OCR text",
                indexedMemory.ocrText
                    .isNotBlank()
            )
        }

    private fun grantGalleryAccess(
        instrumentation:
            android.app.Instrumentation,
        context: Context
    ) {
        val permission =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {
                Manifest.permission
                    .READ_MEDIA_IMAGES
            } else {
                Manifest.permission
                    .READ_EXTERNAL_STORAGE
            }

        instrumentation.uiAutomation
            .grantRuntimePermission(
                context.packageName,
                permission
            )
    }

    private fun createTextFixture(
        context: Context
    ): android.net.Uri {
        val resolver =
            context.contentResolver

        val values =
            ContentValues().apply {
                put(
                    MediaStore.Images.Media
                        .DISPLAY_NAME,
                    "goldbrain-ci-${System.nanoTime()}.png"
                )
                put(
                    MediaStore.Images.Media
                        .MIME_TYPE,
                    "image/png"
                )

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {
                    put(
                        MediaStore.Images.Media
                            .RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/GoldBrainCi"
                    )
                    put(
                        MediaStore.Images.Media
                            .IS_PENDING,
                        1
                    )
                }
            }

        val uri =
            checkNotNull(
                resolver.insert(
                    MediaStore.Images.Media
                        .EXTERNAL_CONTENT_URI,
                    values
                )
            ) {
                "Could not create MediaStore smoke-test fixture"
            }

        val bitmap =
            Bitmap.createBitmap(
                1200,
                360,
                Bitmap.Config.ARGB_8888
            )

        try {
            val canvas =
                Canvas(bitmap)

            canvas.drawColor(
                Color.WHITE
            )

            val paint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {
                    color = Color.BLACK
                    textSize = 128f
                    typeface =
                        Typeface.DEFAULT_BOLD
                }

            canvas.drawText(
                "GOLDBRAIN 12345",
                50f,
                220f,
                paint
            )

            checkNotNull(
                resolver.openOutputStream(
                    uri,
                    "w"
                )
            ).use { output ->
                check(
                    bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        output
                    )
                ) {
                    "Could not encode MediaStore smoke-test fixture"
                }
            }
        } finally {
            bitmap.recycle()
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {
            resolver.update(
                uri,
                ContentValues().apply {
                    put(
                        MediaStore.Images.Media
                            .IS_PENDING,
                        0
                    )
                },
                null,
                null
            )
        }

        return uri
    }

    private companion object {
        const val MODEL_TIMEOUT_MS =
            30_000L

        const val INDEX_TIMEOUT_MS =
            90_000L
    }
}
