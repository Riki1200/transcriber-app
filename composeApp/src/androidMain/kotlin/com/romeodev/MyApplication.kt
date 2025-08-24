package com.romeodev

import android.app.Application
import com.romeodev.core.di.initKoin
import com.romeodev.core.firebase.auth.AuthUtils
import com.romeodev.core.purchases.initRevenueCat
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthUtils.initGoogleAuthProvider()
        initKoin {
            androidLogger()
            androidContext(this@MyApplication)
        }
        initRevenueCat()
    }

}

