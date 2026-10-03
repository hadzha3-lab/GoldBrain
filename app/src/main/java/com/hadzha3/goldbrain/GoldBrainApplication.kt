package com.hadzha3.goldbrain

import android.app.Application
import android.content.Context
import com.hadzha3.goldbrain.di.AppContainer

class GoldBrainApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as GoldBrainApplication).container
