package com.romeodev.core


import cnames.structs.whisper_context
import com.romeodev.decodeWavToFloats
import cnames.structs.*

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cstr
import kotlinx.cinterop.free
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import platform.AudioToolbox.AudioQueueAllocateBuffer
import platform.AudioToolbox.AudioQueueBuffer
import platform.AudioToolbox.AudioQueueBufferRef
import platform.AudioToolbox.AudioQueueBufferRefVar
import platform.AudioToolbox.AudioQueueDispose
import platform.AudioToolbox.AudioQueueEnqueueBuffer
import platform.AudioToolbox.AudioQueueFreeBuffer
import platform.AudioToolbox.AudioQueueInputCallback
import platform.AudioToolbox.AudioQueueNewInput
import platform.AudioToolbox.AudioQueueRef
import platform.AudioToolbox.AudioQueueRefVar
import platform.AudioToolbox.AudioQueueStart
import platform.AudioToolbox.AudioQueueStop
import platform.CoreAudioTypes.AudioStreamBasicDescription
import platform.CoreAudioTypes.AudioStreamPacketDescription
import platform.CoreAudioTypes.AudioTimeStamp
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.CoreAudioTypes.kLinearPCMFormatFlagIsSignedInteger
import platform.CoreFoundation.CFRunLoopGetCurrent
import platform.CoreFoundation.kCFRunLoopCommonModes
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.UIKit.UIDevice
import platform.darwin.ByteVar
import platform.darwin.UInt32
import platform.posix._SC_NPROCESSORS_ONLN
import platform.posix.sysconf
import whisper.WHISPER_SAMPLE_RATE
import whisper.whisper_context_default_params
import whisper.whisper_free
import whisper.whisper_free_state
import whisper.whisper_full
import whisper.whisper_full_default_params
import whisper.whisper_full_get_segment_text
import whisper.whisper_full_get_segment_text_from_state
import whisper.whisper_full_get_token_text_from_state
import whisper.whisper_full_n_segments
import whisper.whisper_full_n_segments_from_state
import whisper.whisper_full_n_tokens_from_state
import whisper.whisper_full_params
import whisper.whisper_full_with_state
import whisper.whisper_init_from_file_with_params
import whisper.whisper_init_state
import whisper.whisper_reset_timings
import whisper.whisper_sampling_strategy
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource


fun bundledWhisperModelPath(name: String = "ggml-tiny", ext: String = "bin"): String? {
    val url = NSBundle.mainBundle.URLForResource(name, ext, subdirectory = "models")
    return url?.path
}


private const val NUM_BUFFERS = 3
private const val NUM_BYTES_PER_BUFFER = 16 * 1024


private class AQHolder(
    val pushI16ToRing: (CPointer<ShortVar>, Int) -> Unit,
    val isRealtime: () -> Boolean
) {
    @Volatile
    var closed = false

    @Volatile
    var isProcessing = false
}

typealias AudioQueueBufferRef = CPointer<ByteVar>


private val audioQueueCallback = staticCFunction { userData: COpaquePointer?,
                                                   inAQ: AudioQueueRef?,
                                                   inBuffer: AudioQueueBufferRef?,
                                                   inStartTime: CPointer<AudioTimeStamp>?,
                                                   inNumPackets: UInt32,
                                                   inPacketDescs: CPointer<AudioStreamPacketDescription>? ->
    try {
        if (userData == null) return@staticCFunction
        val holder = userData.asStableRef<AQHolder>().get()

        if (holder.closed) {
            return@staticCFunction
        }

        if (inBuffer == null) return@staticCFunction

        val byteSize = inBuffer.pointed.mAudioDataByteSize.toInt()
        val nSamples = byteSize / 2

        val audioPtr = inBuffer.pointed.mAudioData?.reinterpret<ShortVar>()
        if (audioPtr != null && nSamples > 0) {
            holder.pushI16ToRing(audioPtr, nSamples)
        }

        if (inAQ != null) AudioQueueEnqueueBuffer(inAQ, inBuffer, 0u, null)
    } catch (t: Throwable) {
        NSLog("AudioQueue callback error: ${t.message}")
    }
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
                maxOf(1, (sysconf(_SC_NPROCESSORS_ONLN)).toInt() - 1)

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


    @OptIn(BetaInteropApi::class, ExperimentalTime::class)
    actual fun startStreaming(
        config: StreamConfig,
        onPartial: (TranscriptResult) -> Unit
    ): StreamHandle {
        val c = requireNotNull(ctx) { "WhisperEngine cerrado" }


        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setMode(AVAudioSessionModeMeasurement, error = null)
        session.setActive(true, error = null)
        if (session.inputGainSettable) session.setInputGain(1.0f, error = null)


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

        fun pushFromI16(ptr: CPointer<ShortVar>, n: Int) {
            val tmp = FloatArray(n)
            for (i in 0 until n) {
                tmp[i] = ptr[i].toInt() / 32768.0f
            }
            pushPcm(tmp)
        }


        val holder = AQHolder(pushI16ToRing = { p, n -> pushFromI16(p, n) }, isRealtime = { false })

        val holderRef = StableRef.create(holder)
        val userDataPtr: COpaquePointer = holderRef.asCPointer()


        val dataFormat = nativeHeap.alloc<AudioStreamBasicDescription>().apply {
            mSampleRate = WHISPER_SAMPLE_RATE.toDouble()
            mFormatID = kAudioFormatLinearPCM
            mFramesPerPacket = 1u
            mChannelsPerFrame = 1u
            mBytesPerFrame = 2u
            mBytesPerPacket = 2u
            mBitsPerChannel = 16u
            mFormatFlags = kLinearPCMFormatFlagIsSignedInteger
            mReserved = 0u
        }


        val queueVar =
            nativeHeap.alloc<CPointerVar<OpaqueAudioQueue>>()
        val status = AudioQueueNewInput(
            dataFormat.ptr,
            audioQueueCallback,
            userDataPtr,
            CFRunLoopGetCurrent(),
            kCFRunLoopCommonModes,
            0u,
            queueVar.ptr
        )


        if (status != 0) {
            holderRef.dispose()
            nativeHeap.free(dataFormat)
            nativeHeap.free(queueVar)
            whisper_free_state(state)
            error("AudioQueueNewInput fallo: $status")
        }

        val queue = queueVar.value ?: run {
            holderRef.dispose()
            nativeHeap.free(dataFormat)
            nativeHeap.free(queueVar)
            whisper_free_state(state)
            error("AudioQueueNewInput devolvió queue null")
        }




        val bufferRefs =
            mutableListOf<CPointer<AudioQueueBuffer>>()
        val bufferPtrList =
            mutableListOf<CPointerVar<AudioQueueBuffer>>()


        for (i in 0 until NUM_BUFFERS) {

            val bufVar = nativeHeap.alloc<CPointerVar<AudioQueueBuffer>>()
            bufVar.value = null


            val allocStatus =
                AudioQueueAllocateBuffer(queue, NUM_BYTES_PER_BUFFER.toUInt(), bufVar.ptr)
            if (allocStatus != 0) {

                for (br in bufferRefs) AudioQueueFreeBuffer(queue, br)
                AudioQueueStop(queue, true)
                AudioQueueDispose(queue, true)
                holderRef.dispose()
                for (pv in bufferPtrList) nativeHeap.free(pv)
                nativeHeap.free(dataFormat)
                nativeHeap.free(queueVar)
                whisper_free_state(state)
                error("AudioQueueAllocateBuffer fallo: $allocStatus")
            }


            val bufRef: CPointer<AudioQueueBuffer> = bufVar.value!!


            AudioQueueEnqueueBuffer(queue, bufRef, 0u, null)


            bufferRefs.add(bufRef)
            bufferPtrList.add(bufVar)
        }


        AudioQueueStart(queue, null)


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

                    samples.usePinned { buf ->
                        memScoped {
                            val params =
                                whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)
                            val p = params.ptr.pointed

                            p.print_realtime = true
                            p.print_progress = false
                            p.print_timestamps = true
                            p.print_special = false
                            p.translate = true
                            p.no_context = true
                            p.single_segment = true
                            p.no_timestamps = p.single_segment


                            val lang = config.language ?: language
                            if (!lang.isNullOrEmpty()) {
                                lang.cstr.getPointer(this).let { p.language = it }
                                p.detect_language = false
                            } else {
                                p.detect_language = true
                            }
                            val cpuCount = (sysconf(_SC_NPROCESSORS_ONLN)).toInt()
                            p.n_threads = maxOf(1, minOf(8, cpuCount - 1))

                            try {
                                holder.isProcessing = true
                                val rc = whisper_full_with_state(
                                    c,
                                    state,
                                    params,
                                    buf.addressOf(0),
                                    samples.size
                                )
                                if (rc != 0) {
                                    NSLog("whisper_full_with_state fallo: $rc")
                                } else {



                                    val nSegments = whisper_full_n_segments_from_state(state)
                                    for (seg in 0 until nSegments) {
                                        val nTokens = whisper_full_n_tokens_from_state(state, seg)

                                        val segmentText = buildString {
                                            for (tok in 0 until nTokens) {
                                                val t = whisper_full_get_token_text_from_state(ctx, state, seg, tok)?.toKString() ?: ""

                                                if (!t.startsWith("[_") && !t.endsWith("_]")) {
                                                    append(t)
                                                }
                                            }
                                        }

                                        val text = segmentText.trim()
                                        if (text.isNotEmpty()) {
                                            onPartial(
                                                TranscriptResult(
                                                    text = text,
                                                    language = lang
                                                )
                                            )
                                        }
                                    }

                                }
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            } finally {
                                holder.isProcessing = false
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

        return IOSStreamHandle(stopImpl = {


            holderRef.get().closed = true

            runBlocking {
                job.cancel()
                try {
                    job.join()
                } catch (_: CancellationException) { /* ignore */ }


                val start = TimeSource.Monotonic.markNow()
                while (holderRef.get().isProcessing) {
                    // Delay pequeño para no bloquear CPU
                    delay(10)
                    // Timeout de seguridad (ej: 2 segundos)
                    if (start.elapsedNow() > 2.seconds) {
                        NSLog("stopImpl: timeout esperando a isProcessing=false")
                        break
                    }
                }
            }


            AudioQueueStop(queue, true)
            for (br in bufferRefs) AudioQueueFreeBuffer(queue, br)
            AudioQueueDispose(queue, true)


            for (pv in bufferPtrList) nativeHeap.free(pv)
            nativeHeap.free(queueVar)
            nativeHeap.free(dataFormat)


            holderRef.dispose()
            whisper_free_state(state)

            session.setActive(false, error = null)
        })
    }




    private class IOSStreamHandle(
        private val stopImpl: () -> Unit
    ) : StreamHandle {
        @Volatile
        private var active = true
        override fun stop() {
            if (active) {
                active = false;
                stopImpl()
            }
        }

        override val isActive: Boolean get() = active
    }
}



