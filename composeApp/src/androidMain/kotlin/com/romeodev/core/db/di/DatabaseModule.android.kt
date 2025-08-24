package com.romeodev.core.db.di

import com.romeodev.core.db.DatabaseProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single {
        DatabaseProvider(
            context = get()
        )
    }
}