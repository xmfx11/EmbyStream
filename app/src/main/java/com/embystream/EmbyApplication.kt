package com.embystream

import android.app.Application
import com.embystream.data.local.TokenManager

class EmbyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
