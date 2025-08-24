package com.romeodev.core.datastore.di

import com.romeodev.core.datastore.common.CommonDataStore
import com.romeodev.core.datastore.onboarding.OnboardingDataStore
import com.romeodev.core.datastore.theme.ThemeDataStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val dataStoreModule = module {
    singleOf(::ThemeDataStore)
    singleOf(::OnboardingDataStore)
//    singleOf(::PurchasesDataStore)
    singleOf(::CommonDataStore)
}