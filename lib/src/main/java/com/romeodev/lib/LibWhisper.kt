package com.romeodev.lib

import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import com.whispercpp.whisper.WhisperCpuConfig
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.lang.RuntimeException
import java.util.concurrent.Executors

private const val LOG_TAG = "NativeWhisper"

class NativeWhisper {
    companion object {
        init {
            val abi = Build.SUPPORTED_ABIS.firstOrNull()
            val cpuInfo = runCatching { File("/proc/cpuinfo").readText() }.getOrNull().orEmpty()
            val loaded = when {
                abi == "arm64-v8a" && "fphp" in cpuInfo -> runCatching { System.loadLibrary("whisper_v8fp16_va"); true }.getOrElse { false }
                abi == "armeabi-v7a" && "vfpv4" in cpuInfo -> runCatching { System.loadLibrary("whisper_vfpv4"); true }.getOrElse { false }
                else -> false
            }
            if (!loaded) System.loadLibrary("whisper") // fallback (tu CMake sí la construye)
            Log.d(LOG_TAG, "Native lib loaded for ABI=$abi")
        }

        @JvmStatic external fun initContext(modelPath: String): Long
        @JvmStatic external fun initContextFromAsset(am: AssetManager, assetPath: String): Long
        @JvmStatic external fun initContextFromInputStream(stream: InputStream): Long
        @JvmStatic external fun freeContext(ctx: Long)
        @JvmStatic external fun fullTranscribe(ctx: Long, threads: Int, audio: FloatArray)
        @JvmStatic external fun getTextSegmentCount(ctx: Long): Int
        @JvmStatic external fun getTextSegment(ctx: Long, i: Int): String
        @JvmStatic external fun getTextSegmentT0(ctx: Long, i: Int): Long
        @JvmStatic external fun getTextSegmentT1(ctx: Long, i: Int): Long
        @JvmStatic external fun getSystemInfo(): String
        @JvmStatic external fun benchMemcpy(n: Int): String
        @JvmStatic external fun benchGgmlMulMat(n: Int): String
    }
}



private fun toTimestamp(t: Long, comma: Boolean = false): String {
    var msec = t * 10
    val hr = msec / (1000 * 60 * 60)
    msec -= hr * (1000 * 60 * 60)
    val min = msec / (1000 * 60)
    msec -= min * (1000 * 60)
    val sec = msec / 1000
    msec -= sec * 1000

    val delimiter = if (comma) "," else "."
    return String.format("%02d:%02d:%02d%s%03d", hr, min, sec, delimiter, msec)
}

private fun isArmEabiV7a(): Boolean {
    return Build.SUPPORTED_ABIS[0].equals("armeabi-v7a")
}

private fun isArmEabiV8a(): Boolean {
    return Build.SUPPORTED_ABIS[0].equals("arm64-v8a")
}
//
private fun cpuInfo(): String? {
    return try {
        File("/proc/cpuinfo").inputStream().bufferedReader().use {
            it.readText()
        }
    } catch (e: Exception) {
        Log.w(LOG_TAG, "Couldn't read /proc/cpuinfo", e)
        null
    }
}