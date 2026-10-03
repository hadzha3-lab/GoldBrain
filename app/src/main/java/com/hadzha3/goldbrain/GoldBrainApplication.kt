package com.hadzha3.goldbrain

import android.app.Application
import android.content.Context
import com.hadzha3.goldbrain.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GoldBrainApplication : Application() {
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
}

val Context.appContainer: AppContainer
    get() =
        (
            applicationContext
                as GoldBrainApplication
            ).container
