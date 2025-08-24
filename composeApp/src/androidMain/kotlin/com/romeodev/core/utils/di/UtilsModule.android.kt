package com.romeodev.core.utils.di

import com.romeodev.core.utils.datastore.AppDataStore
import com.romeodev.core.utils.intents.IntentUtils
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformUtilsModule = module {
    singleOf(::AppDataStore)
    singleOf(::IntentUtils)
}