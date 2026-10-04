package com.hadzha3.goldbrain.background

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager

class AndroidIndexingLoadProvider(
    private val context: Context
) {
    fun current(): IndexingLoadPolicy =
        IndexingLoadPolicySelector
            .select(
                isCharging =
                    isCharging(),
                isPowerSaveMode =
                    isPowerSaveMode()
            )

    private fun isCharging(): Boolean {
        val battery =
            context.registerReceiver(
                null,
                IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED
                )
            )

        val status =
            battery?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) ?: -1

        return status ==
            BatteryManager.BATTERY_STATUS_CHARGING ||
            status ==
            BatteryManager.BATTERY_STATUS_FULL
    }

    private fun isPowerSaveMode(): Boolean {
        val powerManager =
            context.getSystemService(
                PowerManager::class.java
            )

        return powerManager
            ?.isPowerSaveMode
            ?: false
    }
}
