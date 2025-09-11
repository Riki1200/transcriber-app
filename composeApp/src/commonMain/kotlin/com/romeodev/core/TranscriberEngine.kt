package com.romeodev.core




/**
 * This class is used to store the result of the transcription.
 * @param text the transcribed text.
 * @param language the language of the audio file.
 */
data class TranscriptResult(val text: String, val language: String? = null)


/**
 * This class is used to store the source of the audio file.
 * @param absolutePath the path to the audio file.
 * @param pcm the audio file in float format.
 *
 */
sealed class AudioSource {
    data class Path(val absolutePath: String) : AudioSource()
    data class Floats(val pcm: FloatArray) : AudioSource() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Floats

            return pcm.contentEquals(other.pcm)
        }

        override fun hashCode(): Int {
            return pcm.contentHashCode()
        }
    }
}


/** Streaming configuration */
data class StreamConfig(
    val sampleRate: Int = 16_000, // target SR
    val windowSeconds: Int = 10, // analysis window for each tick
    val intervalMs: Long = 500, // how often to push partials
    val detectLanguageOnce: Boolean = true,
    val language: String? = null // if set, disables detection
)


/** Handle to control a streaming session */
interface StreamHandle {
    fun stop()
    val isActive: Boolean
}

/**
 * This class is used to transcribe audio files to text.
 * The constructor takes two parameters:
 * @param modelPath  the path to the model file.
 * @param language  the language of the audio file.
 */

expect class WhisperEngine {

    constructor(modelPath: String?, language: String? = null)


    /**
     * Transcribes the audio file to text.
     * @param source the source of the audio file.
     * @return the transcribed text.
     */

    suspend fun transcribe(source: AudioSource): TranscriptResult

    // NEW — start platform streaming and emit partials via callback.
// Returns a handle so caller can stop().
    fun startStreaming(
        config: StreamConfig = StreamConfig(),
        onPartial: (TranscriptResult) -> Unit
    ): StreamHandle

    /**
     * Closes the engine.
     */
    fun close()
}