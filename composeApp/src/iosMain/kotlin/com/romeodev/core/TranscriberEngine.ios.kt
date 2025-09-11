package com.romeodev.core

import kotlinx.cinterop.*
import platform.Foundation.*
import whisper.*


import cnames.structs.whisper_context
import cnames.structs.whisper_state
import com.romeodev.decodeWavToFloats
import platform.AVFAudio.AVAudioCommonFormat
import kotlinx.cinterop.memScoped
import platform.AVFAudio.*
import platform.AVFAudio.AVAudioConverter
import platform.AVFAudio.AVAudioConverterInputBlock
import platform.AVFAudio.AVAudioConverterInputStatus_HaveData
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.UIKit.UIDevice;
import kotlin.concurrent.Volatile


fun bundledWhisperModelPath(name: String = "ggml-tiny", ext: String = "bin"): String? {
    val url = NSBundle.mainBundle.URLForResource(name, ext, subdirectory = "models")
    return url?.path
}


actual class WhisperEngine actual constructor(
    private val modelPath: String?,
    private val language: String?
) {
    private var ctx: CPointer<whisper_context>? = null

    init {
        memScoped {
            val params = whisper_context_default_params()

            if (UIDevice.currentDevice.model.contains("Simulator")) {
                params.ptr.pointed.use_gpu = false
                params.ptr.pointed.flash_attn = false
            } else {
                params.ptr.pointed.use_gpu = true
                params.ptr.pointed.flash_attn = true
            }


            val path = modelPath ?: bundledWhisperModelPath()
            ?: error("No se encontró el modelo en el bundle (models/ggml-*.bin)")


            ctx = whisper_init_from_file_with_params(path, params)
            require(ctx != null) { "No se pudo cargar el modelo en iOS: $path" }
        }
    }

    actual suspend fun transcribe(source: AudioSource): TranscriptResult {
        val c = requireNotNull(ctx) { "WhisperEngine cerrado" }


        val floats: FloatArray = when (source) {
            is AudioSource.Floats -> source.pcm
            is AudioSource.Path -> decodeWavToFloats(source.absolutePath)
        }

        memScoped {

            val params =
                whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)
            val p = params.ptr.pointed

            p.print_realtime = true
            p.print_progress = false
            p.print_timestamps = true
            p.print_special = false
            p.translate = true
            p.detect_language = true


            language?.let { lang ->
                lang.cstr.getPointer(this).let { p.language = it }
            }
            p.n_threads =
                maxOf(1, (platform.posix.sysconf(platform.posix._SC_NPROCESSORS_ONLN)).toInt() - 1)

            whisper_reset_timings(c)
            floats.usePinned { buf ->
                val rc = whisper_full(c, params, buf.addressOf(0), floats.size)
                require(rc == 0) { "whisper_full falló: $rc" }
            }
        }

        val n = whisper_full_n_segments(c)
        val sb = StringBuilder()
        for (i in 0 until n) {
            sb.append(whisper_full_get_segment_text(c, i)!!.toKString())
        }

        return TranscriptResult(text = sb.toString(), language = language)
    }

    actual fun close() {
        ctx?.let { whisper_free(it) }
        ctx = null
    }


    private class IOSStreamHandle(
        private val stopImpl: () -> Unit
    ) : StreamHandle {
        @Volatile
        private var active = true
        override fun stop() {
            if (active) {
                active = false; stopImpl()
            }
        }

        override val isActive: Boolean get() = active
    }

    actual fun startStreaming(
        config: StreamConfig,
        onPartial: (TranscriptResult) -> Unit
    ): StreamHandle {
        val c = requireNotNull(ctx) { "WhisperEngine cerrado" }

        // 1) Sesión + engine
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setMode(AVAudioSessionModeMeasurement, error = null)
        session.setActive(true, error = null)

        val engine = AVAudioEngine()
        val inputNode = engine.inputNode
        val hwFormat = inputNode.inputFormatForBus(0u)

        // 2) Destino 16 kHz mono Float32 (¡ojo con el enum!)
        val outFormat = AVAudioFormat(
            commonFormat = AVAudioPCMFormatFloat32,
            sampleRate = config.sampleRate.toDouble(),
            channels = 1u,
            interleaved = false
        )!!

        val converter = AVAudioConverter(fromFormat = hwFormat, toFormat = outFormat)

        // 3) Estado whisper + buffers (modo “igual que Obj-C”: acumulamos y procesamos todo)
        val state: CPointer<whisper_state> = whisper_init_state(c)
            ?: error("No se pudo crear whisper_state")

        // Máximo audio acumulado (como el ejemplo: algunos segundos; aquí 30 s máx)
        val maxSeconds = 30
        val maxSamples = maxSeconds * config.sampleRate
        val ring = FloatArray(maxSamples)
        var w = 0               // índice de escritura
        var total = 0           // cuántos samples válidos acumulados (<= maxSamples)

        fun pushPcm(pcm: FloatArray) {
            for (x in pcm) {
                ring[w] = x
                w = (w + 1) % ring.size
                if (total < ring.size) total++
            }
        }


        inputNode.installTapOnBus(0u, 1024u, hwFormat) { buffer, _ ->


            val outBuf = AVAudioPCMBuffer(outFormat, 8192u)
            outBuf.frameLength = 8192u


            val inputBlock: AVAudioConverterInputBlock = { inNumPackets, outStatus ->
                // Limita el tamaño del buffer al solicitado
                val requested = inNumPackets.toInt()

                if (buffer != null) {
                    if (requested > 0 && buffer.frameLength.toInt() > requested) {
                        buffer.frameLength = requested.toUInt()
                    }
                }

                outStatus?.let { it[0] = AVAudioConverterInputStatus_HaveData }

                buffer
            }

            memScoped {
                val err = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
                converter.convertToBuffer(outBuf, error = err.ptr, withInputFromBlock = inputBlock)
                if (err.value != null) {
                    return@installTapOnBus
                }
            }

            val ch0 = outBuf.floatChannelData!![0]!!
            val frames = outBuf.frameLength.toInt()
            val arr = FloatArray(frames)
            for (i in 0 until frames) arr[i] = ch0[i]
            pushPcm(arr)
        }

        engine.prepare()
        engine.startAndReturnError(null)

        // 5) Timer “realtime” al estilo Obj-C: ejecuta whisper_full con parámetros equivalentes
        val timer = NSTimer.scheduledTimerWithTimeInterval(
            config.intervalMs.toDouble() / 1000.0,
            repeats = true
        ) { _ ->
            if (total <= 0) return@scheduledTimerWithTimeInterval

            // Copiamos TODO lo acumulado (como el sample Obj-C) o limita a windowSeconds si quieres
            val take = if (config.windowSeconds > 0)
                minOf(total, config.windowSeconds * config.sampleRate)
            else total

            val samples = FloatArray(take)
            // leer desde w - take (ring buffer)
            var idx = (w - take + ring.size) % ring.size
            for (i in 0 until take) {
                samples[i] = ring[idx]; idx = (idx + 1) % ring.size
            }

            memScoped {
                val params =
                    whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)
                val p = params.ptr.pointed


                p.print_realtime = true
                p.print_progress = false
                p.print_timestamps = true
                p.print_special = false
                p.translate = false

                // Idioma: fijo si viene en config o en el ctor; si no, detectar
                val lang = config.language ?: language
                if (!lang.isNullOrEmpty()) {
                    lang.cstr.getPointer(this).let { p.language = it }
                    p.detect_language = false
                } else {
                    p.detect_language = true
                }

                val maxThreads =
                    maxOf(1, (platform.posix.sysconf(platform.posix._SC_NPROCESSORS_ONLN)).toInt())
                p.n_threads = maxThreads
                p.offset_ms = 0
                p.no_context = true              // como en el sample Obj-C
                p.single_segment = true              // “modo realtime”
                p.no_timestamps = p.single_segment  // igual que Obj-C

                whisper_reset_timings(c)

                samples.usePinned { buf ->
                    val rc =
                        whisper_full_with_state(c, state, params, buf.addressOf(0), samples.size)
                    if (rc != 0) return@usePinned
                }

                // Construimos el texto resultante (todo o lo nuevo)
                val nSeg = whisper_full_n_segments(c)
                if (nSeg > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until nSeg) {
                        val seg = whisper_full_get_segment_text(c, i)?.toKString() ?: ""
                        sb.append(seg)
                    }
                    val text = sb.toString().trim()
                    if (text.isNotEmpty()) {
                        onPartial(TranscriptResult(text = text, language = lang))
                    }
                }
            }
        }

        // 6) Devolver handle que limpia todo al parar
        return IOSStreamHandle {
            timer.invalidate()
            engine.inputNode.removeTapOnBus(0u)
            engine.stop()
            session.setActive(false, error = null)
            whisper_free_state(state)
        }
    }


}