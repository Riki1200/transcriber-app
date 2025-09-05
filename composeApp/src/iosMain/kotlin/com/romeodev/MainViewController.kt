package com.romeodev

import androidx.compose.ui.window.ComposeUIViewController
import com.romeodev.core.di.initKoin
import com.romeodev.core.di.iosCoreModule

import com.romeodev.core.di.whisperModule
import com.romeodev.core.firebase.auth.AuthUtils
import com.romeodev.core.purchases.initRevenueCat



fun mainViewController() = ComposeUIViewController(
    configure = {
        AuthUtils.initGoogleAuthProvider()
        initKoin(
            platformModules = listOf(
                iosCoreModule,
//                whisperModule(defaultLanguage = "en")
            )
        )
        initRevenueCat()
    }
) {
    App()
}