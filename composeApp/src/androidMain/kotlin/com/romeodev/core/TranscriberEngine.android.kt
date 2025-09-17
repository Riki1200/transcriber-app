package com.romeodev.core

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import com.romeodev.helpers.media.decodeWavToFloatsSmart
import com.romeodev.lib.WhisperContext
import kotlinx.coroutines.CoroutineScope


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


private class AndroidStreamHandle(
    private val stopImpl: () -> Unit
) : StreamHandle {
    @Volatile private var active = true

    override fun stop() {
        if (active) {
            active = false
            stopImpl()
        }
    }

    override val isActive: Boolean
        get() = active
}

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

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    actual fun startStreaming(
        config: StreamConfig,
        onPartial: (TranscriptResult) -> Unit
    ): StreamHandle {
        val c = requireNotNull(ctx) { "WhisperEngine cerrado" }

        val sr = config.sampleRate


        val minBuf = AudioRecord.getMinBufferSize(sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sr)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf * 4)
            .build()


        val ring = FloatArray(sr * 30)
        var w = 0
        var lastText = ""
        var job: Job? = null


        fun pushPcm(pcm: FloatArray) {
            for (x in pcm) { ring[w] = x; w = (w + 1) % ring.size }
        }

        recorder.startRecording()


        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())


        job = scope.launch {
            val readBuf = FloatArray(2048)
            val tick = config.intervalMs
            var lastTick = SystemClock.elapsedRealtime()

            while (isActive) {
                val n = recorder.read(readBuf, 0, readBuf.size, AudioRecord.READ_BLOCKING)
                if (n > 0) pushPcm(if (n == readBuf.size) readBuf else readBuf.copyOf(n))


                val now = SystemClock.elapsedRealtime()
                if (now - lastTick >= tick) {
                    lastTick = now

                    val win = (config.windowSeconds * sr)
                    val take = kotlin.math.min(win, ring.size)
                    val samples = FloatArray(take)
                    var idx = (w - take + ring.size) % ring.size
                    for (i in 0 until take) { samples[i] = ring[idx]; idx = (idx + 1) % ring.size }


                    val text = c.transcribeData(
                        samples,
                        printTimestamp = true,
                    )
                    if (text.isNotBlank() && text != lastText) {
                        lastText = text
                        onPartial(TranscriptResult(text, language = config.language))
                    }
                }
            }
        }

        return AndroidStreamHandle {
            job.cancel()
            recorder.stop();
            recorder.release()
        }
    }


}