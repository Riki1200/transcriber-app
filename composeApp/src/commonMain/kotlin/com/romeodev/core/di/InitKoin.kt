package com.romeodev.core.di


import com.romeodev.features.auth.di.authModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration


fun initKoin(
    config: KoinAppDeclaration? = null,
    platformModules: List<Module> = emptyList(),
    defaultLanguage: String? = "en",
    androidContext: Any? = null
) {
    startKoin {

        config?.invoke(this)

        modules(
            coreModule,
            *platformModules.toTypedArray(),
            authModule,
            /*Todo add modules here*/
            viewModels
        )
    }
}