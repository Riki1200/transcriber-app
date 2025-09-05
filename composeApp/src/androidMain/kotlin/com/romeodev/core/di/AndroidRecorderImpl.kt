package com.romeodev.core.di



import android.app.Application
import com.romeodev.features.trancription.domain.models.Recorded
import com.romeodev.features.trancription.domain.models.Recorder

import com.romeodev.helpers.recorder.Recorder as AndroidRecorderDelegate
import java.io.File

class AndroidRecorder(
    private val app: Application,
    private val delegate: AndroidRecorderDelegate = AndroidRecorderDelegate()
) : Recorder {

    private var outFile: File? = null

    override suspend fun start(outputPath: String?) {
        outFile = outputPath?.let { File(it) }
            ?: File.createTempFile("rec-", ".wav", app.cacheDir)

        delegate.startRecording(outFile!!) { e ->
            // aquí puedes loguear o propagar error si quieres
            e.printStackTrace()
        }
    }

    override suspend fun stop(): Recorded {
        delegate.stopRecording()
        val f = requireNotNull(outFile) { "Recorder no inicializado: llama a start() primero" }
        return Recorded(f.absolutePath)
    }
}