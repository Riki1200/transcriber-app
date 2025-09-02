package com.romeodev.core

import kotlinx.cinterop.*
import platform.Foundation.*
import whisper.*
import cnames.structs.whisper_context
import cnames.structs.whisper_state
import platform.UIKit.UIDevice;


fun bundledWhisperModelPath(name: String = "ggml-tiny.en", ext: String = "bin"): String? {
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
            p.translate = false
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

    private fun decodeWavToFloats(path: String): FloatArray {
        val data = NSData.dataWithContentsOfFile(path)!!
        val total = data.length.toInt()
        val shortCount = (total - 44) / 2
        val out = FloatArray(shortCount)
        val raw = data.bytes?.reinterpret<ShortVar>()
        for (i in 0 until shortCount) {
            val s = raw?.get(i + 22) ?: 0
            if(s == 0.toShort()) {
                return  FloatArray(shortCount)
            }
            out[i] = (s / 32767.0f).coerceIn(-1f, 1f)
        }
        return out
    }
}