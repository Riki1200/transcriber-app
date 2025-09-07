package com.romeodev.core.di

import android.app.Application
import android.content.Context
import java.io.File

class AndroidModelPathProvider(
    private val context: Context
) : ModelPathProvider {


    override fun getModelPath(): String? {
        return AndroidModelCopier.ensureModel(
            context.applicationContext,
            assetName = "models/ggml-tiny.bin"
        )
    }
}

object AndroidModelCopier {
    fun ensureModel(app: Context, assetName: String): String {
        val out = File(app.filesDir, assetName.substringAfterLast('/'))
        if (!out.exists()) {
            app.assets.open(assetName).use { inp -> out.outputStream().use { inp.copyTo(it) } }
        }
        return out.absolutePath
    }
}

