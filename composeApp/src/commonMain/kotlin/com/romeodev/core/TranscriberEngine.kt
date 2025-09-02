package com.romeodev.core




data class TranscriptResult(val text: String, val language: String? = null)

sealed class AudioSource {
    data class Path(val absolutePath: String) : AudioSource()
    data class Floats(val pcm: FloatArray) : AudioSource()
}

expect class WhisperEngine {
    /**
     * Crea contexto de modelo y queda listo para transcribir.
     * @param modelPath ruta absoluta a ggml-*.bin (o null si tu iOS lo carga del bundle)
     * @param language ej. "en", "es" (opcional)
     */
    constructor(modelPath: String?, language: String? = null)

    suspend fun transcribe(source: AudioSource): TranscriptResult

    fun close()
}