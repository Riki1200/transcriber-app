package com.romeodev.helpers.media

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder



 object RiffWaveHelper {

     fun decodeWaveFile(file: File): FloatArray {
         val baos = ByteArrayOutputStream()
         file.inputStream().use { it.copyTo(baos) }
         val buffer = ByteBuffer.wrap(baos.toByteArray())
         buffer.order(ByteOrder.LITTLE_ENDIAN)
         val channel = buffer.getShort(22).toInt()
         buffer.position(44)
         val shortBuffer = buffer.asShortBuffer()
         val shortArray = ShortArray(shortBuffer.limit())
         shortBuffer.get(shortArray)
         return FloatArray(shortArray.size / channel) { index ->
             when (channel) {
                 1 -> (shortArray[index] / 32767.0f).coerceIn(-1f..1f)
                 else -> ((shortArray[2 * index] + shortArray[2 * index + 1]) / 32767.0f / 2.0f).coerceIn(
                     -1f..1f
                 )
             }
         }
     }

     fun encodeWaveFile(file: File, data: ShortArray) {
         file.outputStream().use {
             it.write(headerBytes(data.size * 2))
             val buffer = ByteBuffer.allocate(data.size * 2)
             buffer.order(ByteOrder.LITTLE_ENDIAN)
             buffer.asShortBuffer().put(data)
             val bytes = ByteArray(buffer.limit())
             buffer.get(bytes)
             it.write(bytes)
         }
     }
 }



private fun headerBytes(totalLength: Int): ByteArray {
    require(totalLength >= 44)
    ByteBuffer.allocate(44).apply {
        order(ByteOrder.LITTLE_ENDIAN)

        put('R'.code.toByte())
        put('I'.code.toByte())
        put('F'.code.toByte())
        put('F'.code.toByte())

        putInt(totalLength - 8)

        put('W'.code.toByte())
        put('A'.code.toByte())
        put('V'.code.toByte())
        put('E'.code.toByte())

        put('f'.code.toByte())
        put('m'.code.toByte())
        put('t'.code.toByte())
        put(' '.code.toByte())

        putInt(16)
        putShort(1.toShort())
        putShort(1.toShort())
        putInt(16000)
        putInt(32000)
        putShort(2.toShort())
        putShort(16.toShort())

        put('d'.code.toByte())
        put('a'.code.toByte())
        put('t'.code.toByte())
        put('a'.code.toByte())

        putInt(totalLength - 44)
        position(0)
    }.also {
        val bytes = ByteArray(it.limit())
        it.get(bytes)
        return bytes
    }
}


private data class WavInfo(
    val sampleRate: Int,
    val channels: Int,
    val bitsPerSample: Int,
    val dataOffset: Int,
    val dataSize: Int
)

private fun parseWavHeader(bytes: ByteArray): WavInfo {
    fun le16(i: Int) = (bytes[i].toInt() and 0xFF) or ((bytes[i+1].toInt() and 0xFF) shl 8)
    fun le32(i: Int) = (bytes[i].toInt() and 0xFF) or ((bytes[i+1].toInt() and 0xFF) shl 8) or
            ((bytes[i+2].toInt() and 0xFF) shl 16) or ((bytes[i+3].toInt() and 0xFF) shl 24)

    require(String(bytes, 0, 4) == "RIFF" && String(bytes, 8, 4) == "WAVE") { "WAV inválido" }

    var p = 12
    var fmtFound = false
    var dataOffset = -1
    var dataSize = -1
    var audioFormat = 1 // PCM=1, FLOAT=3
    var channels = 1
    var sampleRate = 16000
    var bitsPerSample = 16

    while (p + 8 <= bytes.size) {
        val id = String(bytes, p, 4)
        val size = le32(p + 4)
        val start = p + 8
        if (id == "fmt ") {
            audioFormat = le16(start + 0)
            channels = le16(start + 2)
            sampleRate = le32(start + 4)
            bitsPerSample = le16(start + 14)
            fmtFound = true
        } else if (id == "data") {
            dataOffset = start
            dataSize = size
        }
        p = start + size
        if (p % 2 == 1) p++ // alineación
        if (fmtFound && dataOffset >= 0) break
    }
    require(fmtFound && dataOffset >= 0 && dataSize > 0) { "Chunks WAV no encontrados" }
    return WavInfo(sampleRate, channels, bitsPerSample, dataOffset, dataSize)
}

 fun decodeWavToFloatsSmart(bytes: ByteArray, targetRate: Int = 16000): FloatArray {
    val h = parseWavHeader(bytes)
    val data = bytes.copyOfRange(h.dataOffset, h.dataOffset + h.dataSize)

    // 1) Convertir a float mono [-1,1]
    val mono: FloatArray = when (h.bitsPerSample) {
        16 -> {
            val totalSamples = data.size / 2
            val ch = h.channels
            val out = FloatArray(totalSamples / ch)
            var j = 0
            var i = 0
            while (i + 1 < data.size) {
                // leer int16 LE
                val lo = data[i].toInt() and 0xFF
                val hi = data[i + 1].toInt()
                val s = (hi shl 8) or lo
                val v = (s.toShort() / 32768.0f)
                if (ch == 1) {
                    out[j++] = v
                } else {
                    // si estéreo, promediar L y R
                    val lo2 = data[i+2].toInt() and 0xFF
                    val hi2 = data[i+3].toInt()
                    val s2 = (hi2 shl 8) or lo2
                    val v2 = (s2.toShort() / 32768.0f)
                    out[j++] = 0.5f * (v + v2)
                    i += 2 // consumo extra del segundo canal
                }
                i += 2
            }
            out
        }
        32 -> {
            // 32-bit float WAV
            val totalSamples = data.size / 4
            val ch = h.channels
            val out = FloatArray(totalSamples / ch)
            var j = 0
            var i = 0
            val bb = java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            while (i < totalSamples) {
                val v = bb.float
                val v2 = if (ch > 1) bb.float else v
                out[j++] = if (ch > 1) 0.5f * (v + v2) else v
                i += ch
            }
            out
        }
        else -> error("Bits por muestra no soportados: ${h.bitsPerSample}")
    }

    // 2) Re-muestrear simple a 16 kHz si hace falta (linear)
    if (h.sampleRate == targetRate) return mono
    val ratio = targetRate.toDouble() / h.sampleRate
    val outLen = (mono.size * ratio).toInt().coerceAtLeast(1)
    val res = FloatArray(outLen)
    for (i in 0 until outLen) {
        val srcPos = i / ratio
        val s0 = srcPos.toInt().coerceIn(0, mono.lastIndex)
        val s1 = (s0 + 1).coerceIn(0, mono.lastIndex)
        val t = (srcPos - s0)
        res[i] = mono[s0] * (1 - t).toFloat() + mono[s1] * t.toFloat()
    }
    return res
}