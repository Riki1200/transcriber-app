package com.romeodev.core

import WhisperContext
import com.romeodev.whisper.WhisperLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


actual class WhisperEngine actual constructor(
    private val modelPath: String?,
    private val language: String?
) {
    private var ctx: WhisperContext? = null

    init {

        require(!modelPath.isNullOrEmpty()) { "Android requiere ruta absoluta del modelo" }
        ctx = WhisperContext.createContextFromFile(modelPath)

    }

    actual suspend fun transcribe(source: AudioSource): TranscriptResult =
        withContext(Dispatchers.IO) {
            val c = requireNotNull(ctx) { "WhisperEngine cerrado" }
            val floats: FloatArray = when (source) {
                is AudioSource.Floats -> source.pcm
                is AudioSource.Path -> decodeWavToFloats(File(source.absolutePath))
            }
            val text = c.transcribeData(floats, printTimestamp = true)
            TranscriptResult(text = text, language = language)
        }

    actual fun close() {
        val c = ctx ?: return
        ctx = null

        kotlinx.coroutines.runBlocking { c.release() }
    }


    private fun decodeWavToFloats(file: File): FloatArray {

        val bytes = file.readBytes()
        val out = FloatArray((bytes.size - 44) / 2)
        var j = 0
        var i = 44
        while (i + 1 < bytes.size) {
            val lo = bytes[i].toInt() and 0xFF
            val hi = bytes[i + 1].toInt()
            val s = (hi shl 8) or lo
            out[j++] = (s.toShort() / 32767.0f).coerceIn(-1f, 1f)
            i += 2
        }
        return out
    }
}