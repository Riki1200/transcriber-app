// core/presentation/TranscribeViewModel.kt (shared)
package com.romeodev.features.trancription.presentation.viewModels

import androidx.lifecycle.ViewModel
import com.romeodev.core.*
import com.romeodev.features.trancription.domain.models.Recorder

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.mp.KoinPlatform.getKoin

class TranscribeViewModel(
    private val engine: WhisperEngine = getKoin().get(),
    private val recorder: Recorder = getKoin().get()
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _transcription = MutableStateFlow<TranscriptResult?>(null)
    val transcriber = _transcription.asStateFlow()


    private val _live = MutableStateFlow("")
    val live = _live.asStateFlow()


    fun startRecording() {
        scope.launch {
            try {
                recorder.start()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun stopAndTranscribe() {
        scope.launch {
            try {
                val rec = recorder.stop()

                val res = engine.transcribe(AudioSource.Path(rec.wavPath))
                println("res: $res")


                _transcription.emit(res)


            } catch (t: Throwable) {

            }
        }
    }


    private var handle: StreamHandle? = null


    fun startStreaming() {
        if (handle?.isActive == true) return
        handle = engine.startStreaming(StreamConfig()) { partial ->

            println("partial: $partial")
            scope.launch { _live.emit(partial.text) }
        }
    }


    fun stopStreaming() {
        handle?.stop()
        handle = null
    }

    override fun onCleared() {
        super.onCleared()
        stopStreaming()
        try {
            engine.close()
        } catch (_: Throwable) {}
        scope.cancel()
    }

}
