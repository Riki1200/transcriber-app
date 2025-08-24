package com.romeodev.core.db.di

import com.romeodev.core.db.KmpStarterDatabase
import com.romeodev.core.db.getKmpDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDatabaseModule: Module

val databaseModule = module {
    includes(platformDatabaseModule)
    single<KmpStarterDatabase> {
        getKmpDatabase(
            databaseProvider = get()
        )
    }
}

