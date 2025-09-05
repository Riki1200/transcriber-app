package com.romeodev.features.auth.di

import com.romeodev.features.auth.data.repository.AuthRepositoryImpl
import com.romeodev.features.auth.domain.repository.AuthRepository
import com.romeodev.features.auth.presentation.viewmodels.AuthViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single {
        AuthViewModel(
            repository = get(),
            navigator = get(),
        )
    }
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
}