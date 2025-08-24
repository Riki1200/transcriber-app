package com.romeodev.core.events.di

import com.romeodev.core.datastore.onboarding.OnboardingDataStore
import com.romeodev.core.datastore.theme.ThemeDataStore
import com.romeodev.core.events.navigator.DefaultNavigator
import com.romeodev.core.events.navigator.interfaces.Navigator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val eventsModule = module {
    singleOf(::ThemeDataStore)
    singleOf(::OnboardingDataStore)
    singleOf(::DefaultNavigator).bind<Navigator>()
}