package com.romeodev.core.di

import com.romeodev.features.trancription.domain.models.Recorded
import com.romeodev.features.trancription.domain.models.Recorder
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsBigEndianKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVLinearPCMIsNonInterleaved
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@OptIn(ExperimentalForeignApi::class)
class IosRecorder : Recorder {
    private var recorder: AVAudioRecorder? = null
    private var outUrl: NSURL? = null


    @OptIn(ExperimentalForeignApi::class)
    override suspend fun start(outputPath: String?) {

        val granted = suspendCoroutine { cont ->
            AVAudioSession.sharedInstance().requestRecordPermission { ok ->
                cont.resume(ok)
            }
        }
        require(granted) { "Se requiere permiso de micrófono en iOS" }

        val session = AVAudioSession.sharedInstance()
        memScoped {
            session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                error = null
            )
            session.setActive(true, error = null)
        }


        val path = outputPath ?: run {
            val docs = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            ).first() as String
            "$docs/recording.wav"
        }
        val url = NSURL.fileURLWithPath(path)
        outUrl = url


        val settings: Map<Any?, Any?> = mapOf(
            AVFormatIDKey to kAudioFormatLinearPCM,   // PCM lineal
            AVSampleRateKey to 16000.0,               // 16 kHz
            AVNumberOfChannelsKey to 1,               // mono
            AVLinearPCMBitDepthKey to 16,             // 16-bit
            AVLinearPCMIsFloatKey to false,           // entero, no float
            AVLinearPCMIsBigEndianKey to false,       // little-endian
            AVLinearPCMIsNonInterleaved to false      // interleaved
        )

        memScoped {

            val rec = AVAudioRecorder(
                url,
                settings,
                null
            )

            recorder = rec
            require(recorder!!.prepareToRecord()) { "prepareToRecord() falló" }
            require(recorder!!.record()) { "No se pudo iniciar la grabación" }
        }
    }

    override suspend fun stop(): Recorded {
        val rec = requireNotNull(recorder) { "Recorder no iniciado" }
        rec.stop()
        AVAudioSession.sharedInstance().setActive(false, error = null)
        recorder = null
        val url = requireNotNull(outUrl) { "No hay archivo de salida" }
        return Recorded(url.path!!)
    }
}