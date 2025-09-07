package com.romeodev.core.di

import android.app.Application
import com.romeodev.core.WhisperEngine
import com.romeodev.features.trancription.domain.models.Recorder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidCoreModule = module {
    single {

        val app = androidContext()

        val modelPath =  AndroidModelPathProvider(app)
        WhisperEngine(modelPath = modelPath.getModelPath(), language = "auto")
    }
    single<Recorder> { AndroidRecorder(androidContext() as Application) }
}


//val androidModule = module {
//    single<Recorder> { AndroidRecorder(androidContext() as Application) }
//
//    single<ModelPathProvider> { AndroidModelPathProvider(androidContext()) }
//}