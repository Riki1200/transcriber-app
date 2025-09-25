package com.romeodev.core.db.di


import com.romeodev.core.AppConstants
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.dsl.module


val databaseModule = module {
    val config = RealmConfiguration.Builder(
        schema = setOf()
    )
        .name(AppConstants.DB_NAME)
        .schemaVersion(1)
        .deleteRealmIfMigrationNeeded()
        .build()


    val realm = Realm.open(config)

    println("db name: ${realm.configuration.name}")

    single { realm }
}

