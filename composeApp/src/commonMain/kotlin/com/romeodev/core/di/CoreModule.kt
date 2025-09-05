package com.romeodev.core.di

import com.romeodev.core.datastore.di.dataStoreModule
import com.romeodev.core.db.di.databaseModule
import com.romeodev.core.events.di.eventsModule
import com.romeodev.core.ktor.di.ktorModule
import com.romeodev.core.presentation.TranscribeViewModel
import com.romeodev.core.purchases.di.purchasesModule
import com.romeodev.core.utils.di.utilsModule
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel


val viewModels = module {
    viewModel { TranscribeViewModel(get(), get()) }
}

val coreModule = module {
    includes(
        utilsModule,
        databaseModule,
        eventsModule,
        dataStoreModule,
        purchasesModule,
        ktorModule
    )
}