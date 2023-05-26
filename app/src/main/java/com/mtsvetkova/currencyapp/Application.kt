package com.mtsvetkova.currencyapp

import android.app.Application
import com.mtsvetkova.currencyapp.data.storage.StorageManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Application : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        StorageManager.init(this)
        GlobalScope.launch { StorageManager.clearOutdatedTimeSeries() }
    }
}