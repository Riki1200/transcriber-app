package com.romeodev.core.di

import com.romeodev.core.WhisperEngine
import org.koin.dsl.module

fun whisperModule(defaultLanguage: String? = null) = module {
    single {
        val provider = get<ModelPathProvider>()
        WhisperEngine(
            modelPath = provider.getModelPath(),
            language  = defaultLanguage
        )
    }
}