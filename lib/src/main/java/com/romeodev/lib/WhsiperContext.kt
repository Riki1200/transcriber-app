package com.romeodev.lib

import android.content.res.AssetManager
import android.util.Log
import com.whispercpp.whisper.WhisperCpuConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.Executors

private const val LOG_TAG = "WhisperContext"

class WhisperContext private constructor(private var ptr: Long) {
    private val scope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    suspend fun transcribeData(data: FloatArray, printTimestamp: Boolean = true): String =
        withContext(scope.coroutineContext) {
            require(ptr != 0L)
            val numThreads = WhisperCpuConfig.preferredThreadCount
            Log.d(LOG_TAG, "Selecting $numThreads threads")
            NativeWhisper.fullTranscribe(ptr, numThreads, data)
            val textCount = NativeWhisper.getTextSegmentCount(ptr)
            Log.d(LOG_TAG, "Found $textCount text segments")
            buildString {
                for (i in 0 until textCount) {
                    if (printTimestamp) {
                        val t0 = NativeWhisper.getTextSegmentT0(ptr, i)
                        val t1 = NativeWhisper.getTextSegmentT1(ptr, i)
                        append("[${toTimestamp(t0)} --> ${toTimestamp(t1)}]: ${NativeWhisper.getTextSegment(ptr, i)}\n")
                    } else {
                        append(NativeWhisper.getTextSegment(ptr, i))
                    }
                }
            }
        }

    suspend fun release() = withContext(scope.coroutineContext) {
        if (ptr != 0L) { NativeWhisper.freeContext(ptr); ptr = 0 }
    }

    protected fun finalize() { runBlocking { release() } }

    companion object {
        fun createContextFromFile(filePath: String): WhisperContext {
            val ptr = NativeWhisper.initContext(filePath)
            if (ptr == 0L) throw RuntimeException("Couldn't create context with path $filePath")
            return WhisperContext(ptr)
        }
        fun createContextFromInputStream(stream: InputStream): WhisperContext {
            val ptr = NativeWhisper.initContextFromInputStream(stream)
            if (ptr == 0L) throw RuntimeException("Couldn't create context from input stream")
            return WhisperContext(ptr)
        }
        fun createContextFromAsset(assetManager: AssetManager, assetPath: String): WhisperContext {
            val ptr = NativeWhisper.initContextFromAsset(assetManager, assetPath)
            if (ptr == 0L) throw RuntimeException("Couldn't create context from asset $assetPath")
            return WhisperContext(ptr)
        }
        fun getSystemInfo(): String = NativeWhisper.getSystemInfo()
    }
}

// helper
private fun toTimestamp(t: Long, comma: Boolean = false): String {
    var msec = t * 10
    val hr = msec / (1000 * 60 * 60); msec -= hr * (1000 * 60 * 60)
    val min = msec / (1000 * 60);     msec -= min * (1000 * 60)
    val sec = msec / 1000;            msec -= sec * 1000
    val delimiter = if (comma) "," else "."
    return String.format("%02d:%02d:%02d%s%03d", hr, min, sec, delimiter, msec)
}