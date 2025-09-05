package com.romeodev

import android.app.Application
import com.romeodev.core.di.androidCoreModule
//import com.romeodev.core.di.androidModule
import com.romeodev.core.di.initKoin
import com.romeodev.core.di.whisperModule
import com.romeodev.core.firebase.auth.AuthUtils
import com.romeodev.core.purchases.initRevenueCat
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthUtils.initGoogleAuthProvider()
        initKoin(
            config = {
                androidLogger()
                androidContext(this@MyApplication)
            },
            platformModules = listOf(
                androidCoreModule,
              // whisperModule(defaultLanguage = "en")
            ),
            androidContext = this
        )


        initRevenueCat()
    }

}

