// core/presentation/TranscribeViewModel.kt (shared)
package com.romeodev.core.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var log by mutableStateOf("")
        private set

    fun startRecording() {
        scope.launch {
            try {
                recorder.start()
                append("Grabando...\n")
            } catch (t: Throwable) {
                append("Error start: ${t.message}\n")
            }
        }
    }

    fun stopAndTranscribe() {
        scope.launch {
            try {
                val rec = recorder.stop()
                append("WAV: ${rec.wavPath}\nTranscribiendo...\n")
                val res = engine.transcribe(AudioSource.Path(rec.wavPath))
                println("res: $res")
                append("Texto: ${res.text}\n")
            } catch (t: Throwable) {
                append("Error stop/transcribe: ${t.message}\n")
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private fun append(s: String) {
        log += s
    }
}
