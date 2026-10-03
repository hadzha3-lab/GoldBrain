package com.hadzha3.goldbrain.feature.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hadzha3.goldbrain.core.ui.GoldBrainTheme

class CameraActivity : ComponentActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            GoldBrainTheme {
                CameraScreen(
                    onSaved = ::finishWithResult,
                    onClose = ::finish
                )
            }
        }
    }

    private fun finishWithResult(
        uri: Uri
    ) {
        setResult(
            RESULT_OK,
            Intent().setData(uri)
        )
        finish()
    }
}
