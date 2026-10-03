package com.hadzha3.goldbrain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hadzha3.goldbrain.core.ui.GoldBrainTheme
import com.hadzha3.goldbrain.feature.home.HomeRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            GoldBrainTheme {
                HomeRoute()
            }
        }
    }
}
