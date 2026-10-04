package com.hadzha3.goldbrain

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import com.hadzha3.goldbrain.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GoldBrainApplication :
    Application(),
    SingletonImageLoader.Factory {
    lateinit var container: AppContainer
        private set

    private val applicationScope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default
        )

    override fun onCreate() {
        super.onCreate()

        container =
            AppContainer(this)

        applicationScope.launch {
            runCatching {
                container
                    .indexRecoveryCoordinator
                    .recover()
            }
        }
    }

    override fun newImageLoader(
        context: Context
    ): ImageLoader =
        ImageLoader.Builder(
            context
        )
            .diskCache(
                null
            )
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(
                        context,
                        0.08
                    )
                    .build()
            }
            .build()
}

val Context.appContainer: AppContainer
    get() =
        (
            applicationContext
                as GoldBrainApplication
            ).container
