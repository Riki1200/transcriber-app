package com.romeodev.whisper

import androidx.annotation.Keep
import android.os.Build
import android.util.Log
import java.io.File

private const val LOG_TAG = "LibWhisper"

internal class WhisperLib {
    companion object {
        init {

            try {

                System.loadLibrary("whisper")
                Log.d(LOG_TAG, "Loaded libwhisper.so")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(
                    LOG_TAG,
                    "libwhisper.so not found; make sure you link statically in whisper_jni, or include it",
                    e
                )
            }

            System.loadLibrary("whisper_jni")
            Log.d(LOG_TAG, "Loaded whisper_jni")
        }

        // Métodos nativos (coinciden con los del C++)
        @JvmStatic
        external fun initContext(modelPath: String): Long
        @JvmStatic
        external fun freeContext(contextPtr: Long)
        @JvmStatic
        external fun fullTranscribe(contextPtr: Long, numThreads: Int, audioData: FloatArray)

        @JvmStatic
        external fun getTextSegmentCount(contextPtr: Long): Int
        @JvmStatic
        external fun getTextSegment(contextPtr: Long, index: Int): String
        @JvmStatic
        external fun getTextSegmentT0(contextPtr: Long, index: Int): Long
        @JvmStatic
        external fun getTextSegmentT1(contextPtr: Long, index: Int): Long

        @JvmStatic
        external fun getSystemInfo(): String
        @JvmStatic
        external fun benchMemcpy(nthreads: Int): String
        @JvmStatic
        external fun benchGgmlMulMat(nthreads: Int): String
    }
}
