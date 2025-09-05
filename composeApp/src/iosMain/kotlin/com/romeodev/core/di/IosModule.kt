package com.romeodev.core.di

import com.romeodev.core.WhisperEngine
import com.romeodev.core.bundledWhisperModelPath
import com.romeodev.features.trancription.domain.models.Recorder
import org.koin.dsl.module

val iosCoreModule = module {

    single {
        val modelPath = bundledWhisperModelPath(name = "ggml-tiny.en", ext = "bin")
            ?: error("No se encontró el modelo en models/*.bin del bundle")

        WhisperEngine(modelPath = modelPath, language = "en")
    }

    // Recorder multiplataforma para iOS
    single<Recorder> { IosRecorder() } // tu implementación expect/actual de Recorder en iOS
}



//val iosModule = module {
//    single<ModelPathProvider> { IosModelPathProvider() }
//}