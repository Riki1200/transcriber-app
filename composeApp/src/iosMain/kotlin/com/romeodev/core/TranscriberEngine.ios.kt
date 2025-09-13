package com.romeodev.core

import kotlinx.cinterop.*
import platform.Foundation.*
import whisper.*


import cnames.structs.whisper_context
import cnames.structs.whisper_state
import com.romeodev.decodeWavToFloats
import kotlinx.atomicfu.atomic
import platform.AVFAudio.AVAudioCommonFormat
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
import platform.QuartzCore.CACurrentMediaTime
import platform.UIKit.UIDevice;
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException


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

    private fun buildDefaultParams(mem: MemScope, configureCallback: (whisper_full_params) -> Unit = {}): CPointer<whisper_full_params> {
        val params =
            whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)

        var pointer: CPointer<whisper_full_params>


        memScoped {
            val p = params.ptr.pointed
            configureCallback(p)
            pointer = params.ptr
        }

        return pointer
    }



    actual fun startStreaming(
        config: StreamConfig,
        onPartial: (TranscriptResult) -> Unit
    ): StreamHandle {
        val c = requireNotNull(ctx) { "WhisperEngine cerrado" }

        // Session + engine (igual)
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setMode(AVAudioSessionModeMeasurement, error = null)
        session.setActive(true, error = null)

        val engine = AVAudioEngine()
        val inputNode = engine.inputNode
        val hwFormat = inputNode.inputFormatForBus(0u)

        val outFormat = AVAudioFormat(
            commonFormat = AVAudioPCMFormatFloat32,
            sampleRate = config.sampleRate.toDouble(),
            channels = 1u,
            interleaved = false
        )

        val converter = AVAudioConverter(fromFormat = hwFormat, toFormat = outFormat)

        val state = whisper_init_state(c) ?: error("No se pudo crear whisper_state")


        val maxSamples = 30 * config.sampleRate
        val ring = FloatArray(maxSamples)
         var w = 0
        var total = 0

        fun pushPcm(pcm: FloatArray) {
            for (x in pcm) {
                ring[w] = x
                w = (w + 1) % ring.size
                if (total < ring.size) total++
            }
        }


        inputNode.installTapOnBus(0u, 1024u, hwFormat) { buffer, _ ->
            try {
                if (buffer == null) return@installTapOnBus

                val outBuf = AVAudioPCMBuffer(outFormat, buffer.frameCapacity)!!

                val inputBlock: AVAudioConverterInputBlock = { inNumPackets, outStatus ->
                    outStatus?.pointed?.value = AVAudioConverterInputStatus_HaveData
                    buffer
                }
                memScoped {
                    val err = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
                    val rc = converter.convertToBuffer(outBuf, error = err.ptr, withInputFromBlock = inputBlock)
                    if (err.value != null) {

                        return@installTapOnBus
                    }
                    val frames = outBuf.frameLength.toInt()
                    val src = outBuf.floatChannelData?.get(0)
                    if (src != null) {
                        val arr = FloatArray(frames)
                        for (i in 0 until frames) arr[i] = src[i]
                        pushPcm(arr)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        engine.prepare()
        engine.startAndReturnError(null)

        var lastSeenSegment = 0


        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())



        val job = scope.launch {
            try {
                while (isActive) {
                    if (total <= 0) {
                        delay(config.intervalMs)
                        continue
                    }


                    val take = if (config.windowSeconds > 0) minOf(total, config.windowSeconds * config.sampleRate) else total
                    val samples = FloatArray(take)

                    var idx = (w - take + ring.size) % ring.size
                    for (i in 0 until take) {
                        samples[i] = ring[idx]
                        idx = (idx + 1) % ring.size
                    }


                    memScoped {
                        val params = whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)
                        val p = params.ptr.pointed

                        p.print_realtime = true
                        p.print_progress = false
                        p.print_timestamps = true
                        p.print_special = false
                        p.translate = false
                        p.no_context = true
                        p.single_segment = true
                        p.no_timestamps = p.single_segment
                        p.offset_ms = 0

                        val lang = config.language ?: language
                        if (!lang.isNullOrEmpty()) {
                            lang.cstr.getPointer(this).let { p.language = it }
                            p.detect_language = true
                        } else {
                            p.detect_language = false
                        }
                        p.n_threads = maxOf(1, (platform.posix.sysconf(platform.posix._SC_NPROCESSORS_ONLN)).toInt() - 1)

                        samples.usePinned { buf ->
                            val rc = whisper_full_with_state(c, state, params, buf.addressOf(0), samples.size)
                            if (rc != 0) {
                                // puedes loggear rc
                            } else {

                                val n = whisper_full_n_segments(c)
                                if (n > lastSeenSegment) {
                                    val sb = StringBuilder()
                                    for (i in lastSeenSegment until n) {
                                        sb.append(whisper_full_get_segment_text(c, i)?.toKString() ?: "")
                                    }
                                    lastSeenSegment = n
                                    val text = sb.toString().trim()
                                    if (text.isNotEmpty()) {
                                        onPartial(TranscriptResult(text = text, language = lang))
                                    }
                                }
                            }
                        }
                    }

                    delay(config.intervalMs)
                }
            } catch (_: CancellationException) {

            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        // Stop handler
        return IOSStreamHandle(stopImpl = {
            job.cancel()
            engine.inputNode.removeTapOnBus(0u)
            engine.stop()
            session.setActive(false, error = null)
            whisper_free_state(state)
        })
    }


}