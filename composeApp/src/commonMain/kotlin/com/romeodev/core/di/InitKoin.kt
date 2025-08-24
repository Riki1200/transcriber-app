package com.romeodev.core.di

import com.romeodev.starter_features.auth.di.authModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreModule,
            authModule,
            /*Todo add modules here*/
        )
    }
}