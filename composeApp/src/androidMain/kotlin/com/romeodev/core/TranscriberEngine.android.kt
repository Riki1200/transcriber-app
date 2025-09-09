package com.romeodev.core

import com.romeodev.helpers.media.decodeWavToFloatsSmart
import com.romeodev.lib.WhisperContext


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class WhisperEngine actual constructor(
    modelPath: String?,
    private val language: String?
) {
    private var ctx: WhisperContext? = null

    private fun decodeWavToFloats(file: File): FloatArray =
        decodeWavToFloatsSmart(file.readBytes(), targetRate = 16000)

    init {


        require(!modelPath.isNullOrEmpty()) { "Android requiere ruta absoluta del modelo" }
        ctx =  WhisperContext.createContextFromFile( modelPath)

    }

    actual suspend fun transcribe(source: AudioSource): TranscriptResult =
        withContext(Dispatchers.IO) {
            val c = requireNotNull(ctx) { "WhisperEngine cerrado" }
            val floats: FloatArray = when (source) {
                is AudioSource.Floats -> source.pcm
                is AudioSource.Path -> decodeWavToFloats(File(source.absolutePath))
            }
            val text = c.transcribeData(floats, printTimestamp = true)
            println("Transcription: $text")
            TranscriptResult(text = text, language = language)
        }

    actual fun close() {
        val c = ctx ?: return
        ctx = null

        kotlinx.coroutines.runBlocking { c.release() }
    }


}