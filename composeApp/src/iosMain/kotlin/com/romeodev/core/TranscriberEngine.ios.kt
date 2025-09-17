package com.romeodev.core


import cnames.structs.whisper_context
import com.romeodev.decodeWavToFloats
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioConverter
import platform.AVFAudio.AVAudioConverterInputBlock
import platform.AVFAudio.AVAudioConverterInputStatus_HaveData
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioInputNode
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVAudioPCMFormatFloat64
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsBigEndianKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVLinearPCMIsNonInterleaved
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.inputGainSettable
import platform.AVFAudio.setActive
import platform.AVFAudio.setInputGain
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.UIKit.UIDevice
import whisper.whisper_context_default_params
import whisper.whisper_free
import whisper.whisper_free_state
import whisper.whisper_full
import whisper.whisper_full_default_params
import whisper.whisper_full_get_segment_text
import whisper.whisper_full_get_segment_text_from_state
import whisper.whisper_full_n_segments
import whisper.whisper_full_n_segments_from_state
import whisper.whisper_full_params
import whisper.whisper_full_with_state
import whisper.whisper_init_from_file_with_params
import whisper.whisper_init_state
import whisper.whisper_reset_timings
import whisper.whisper_sampling_strategy
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
            p.no_context = false
            p.single_segment = false
            p.no_timestamps = p.single_segment
            p.offset_ms = 0




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

    private fun buildDefaultParams(
        mem: MemScope,
        configureCallback: (whisper_full_params) -> Unit = {}
    ): CPointer<whisper_full_params> {
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


    @OptIn(BetaInteropApi::class)
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

        if (session.inputGainSettable) {
            session.setInputGain(1.0f, error = null)
        }

        val engine = AVAudioEngine()


        val inputNode = engine.inputNode


        val hwFormat = inputNode.inputFormatForBus(0u)

        val outFormat = AVAudioFormat(
            commonFormat = AVAudioPCMFormatFloat32,
            sampleRate = config.sampleRate.toDouble(),
            channels = 1u,
            interleaved = false,

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
                    val rc = converter.convertToBuffer(
                        outBuf,
                        error = err.ptr,
                        withInputFromBlock = inputBlock
                    )

                    if (err.value != null) {

                        return@installTapOnBus
                    }


                    val frames = outBuf.frameLength.toInt()
                    val src = outBuf.floatChannelData?.get(0)
                    if (src != null) {
                        val arr = FloatArray(frames)
                        var sum: Float = 0f
                        for (i in 0 until frames) {
                            arr[i] = src[i]
                            sum += arr[i] * arr[i] // Calcula la energía del audio
                        }
                        val energy = kotlin.math.sqrt(sum / frames)
                        println("Energía del buffer: $energy") // Imprime un valor que indica el volumen
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

                    val take = if (config.windowSeconds > 0) minOf(
                        total,
                        config.windowSeconds * config.sampleRate
                    ) else total
                    val samples = FloatArray(take)

                    var idx = (w - take + ring.size) % ring.size
                    for (i in 0 until take) {
                        samples[i] = ring[idx]
                        idx = (idx + 1) % ring.size
                    }

                    // Fija el array en la memoria para el bloque de transcripción.
                    // Esto es lo más importante para evitar el EXC_BAD_ACCESS.
                    samples.usePinned { buf ->
                        memScoped {
                            // Creamos los parámetros de la función de C dentro del memScoped.
                            val params = whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)
                            val p = params.ptr.pointed

                            p.print_realtime = true
                            p.print_progress = false
                            p.print_timestamps = true
                            p.print_special = false
                            p.translate = true
                            p.no_context = false // ¡Importante para streaming!
                            p.single_segment = false // ¡Importante para streaming!
                            p.no_timestamps = p.single_segment
                            p.offset_ms = 0
                            p.suppress_blank = true


                            val lang = config.language ?: language
                            if (!lang.isNullOrEmpty()) {
                                lang.cstr.getPointer(this).let { p.language = it }
                                p.detect_language = false
                            } else {
                                p.detect_language = true
                            }
                            val cpuCount = (platform.posix.sysconf(platform.posix._SC_NPROCESSORS_ONLN)).toInt()
                            p.n_threads = maxOf(1, minOf(8, cpuCount - 1))

                            // La llamada a la función de C ahora está en un entorno controlado.
                            val rc = whisper_full_with_state(c, state, params, buf.addressOf(0), samples.size)

                            if (rc != 0) {
                                NSLog("whisper_full_with_state falló: $rc")
                            } else {
                                val n = whisper_full_n_segments_from_state(state) // Ojo con esta función
                                println("current state $n")
                                if (n > lastSeenSegment) {
                                    val sb = StringBuilder()
                                    for (i in lastSeenSegment until n) {
                                        // Usamos las funciones "from_state"
                                        sb.append(whisper_full_get_segment_text_from_state(state, i)?.toKString() ?: "")
                                    }
                                    println("sb $sb")
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