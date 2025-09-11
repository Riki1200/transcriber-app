package com.romeodev

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

fun decodeWavToFloats(path: String): FloatArray {
    val data = NSData.dataWithContentsOfFile(path)
        ?: error("No se pudo leer el archivo: $path")
    val total = data.length.toInt()
    require(total >= 44) { "WAV demasiado corto" }

    fun u8(off: Int) = data.bytes!!.reinterpret<ByteVar>()[off].toInt() and 0xFF
    fun le16(off: Int): Int = u8(off) or (u8(off + 1) shl 8)
    fun le32(off: Int): Int =
        u8(off) or (u8(off + 1) shl 8) or (u8(off + 2) shl 16) or (u8(off + 3) shl 24)

    // RIFF/WAVE
    require(('R'.code == u8(0) && 'I'.code == u8(1) && 'F'.code == u8(2) && 'F'.code == u8(3))) { "No es RIFF" }
    require(('W'.code == u8(8) && 'A'.code == u8(9) && 'V'.code == u8(10) && 'E'.code == u8(11))) { "No es WAVE" }

    // busca fmt  y data
    var off = 12
    var fmtFound = false
    var dataFound = false
    var audioFormat = 1
    var numChannels = 1
    var sampleRate = 16000
    var bitsPerSample = 16
    var dataOffset = -1
    var dataSize = -1

    while (off + 8 <= total) {
        val id = charArrayOf(
            u8(off).toChar(),
            u8(off + 1).toChar(),
            u8(off + 2).toChar(),
            u8(off + 3).toChar()
        ).concatToString()
        val size = le32(off + 4)
        val next = off + 8 + size
        if (next > total) break
        when (id) {
            "fmt " -> {
                fmtFound = true
                audioFormat = le16(off + 8)           // 1=PCM16, 3=Float32
                numChannels = le16(off + 10)          // 1/2
                sampleRate = le32(off + 12)
                bitsPerSample = le16(off + 22)
            }

            "data" -> {
                dataFound = true
                dataOffset = off + 8
                dataSize = size
            }
        }
        off = next + (size % 2) // padding si odd
    }

    require(fmtFound) { "WAV sin chunk fmt " }
    require(dataFound) { "WAV sin chunk data" }
    require(numChannels in 1..2) { "Canales no soportados: $numChannels" }

    val floatsMono: FloatArray = when (audioFormat) {
        1 -> { // PCM16
            require(bitsPerSample == 16) { "Solo PCM16 soportado (bits=$bitsPerSample)" }
            val bytesPerFrame = 2 * numChannels
            val nFrames = dataSize / bytesPerFrame
            val out = FloatArray(nFrames)
            var p = dataOffset
            for (i in 0 until nFrames) {
                if (numChannels == 1) {
                    val s = (le16(p).toShort()).toFloat() / 32768f
                    out[i] = s.coerceIn(-1f, 1f)
                    p += 2
                } else {
                    val l = (le16(p).toShort()).toFloat() / 32768f
                    val r = (le16(p + 2).toShort()).toFloat() / 32768f
                    out[i] = ((l + r) * 0.5f).coerceIn(-1f, 1f)
                    p += 4
                }
            }
            out
        }

        3 -> { // Float32
            require(bitsPerSample == 32) { "Float32 esperado (bits=$bitsPerSample)" }
            fun f32(o: Int): Float {
                val b0 = u8(o)
                val b1 = u8(o + 1)
                val b2 = u8(o + 2)
                val b3 = u8(o + 3)
                val intBits = b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
                return Float.fromBits(intBits) // ✅ disponible en Kotlin/Native
            }

            val bytesPerFrame = 4 * numChannels
            val nFrames = dataSize / bytesPerFrame
            val out = FloatArray(nFrames)
            var p = dataOffset
            for (i in 0 until nFrames) {
                if (numChannels == 1) {
                    out[i] = f32(p).coerceIn(-1f, 1f)
                    p += 4
                } else {
                    val l = f32(p)
                    val r = f32(p + 4)
                    out[i] = ((l + r) * 0.5f).coerceIn(-1f, 1f)
                    p += 8
                }
            }
            out
        }

        else -> error("Formato WAV no soportado (audioFormat=$audioFormat)")
    }

    // resample simple a 16k si hace falta
    return if (sampleRate == 16000) floatsMono else resampleLinear(floatsMono, sampleRate, 16000)
}

fun resampleLinear(input: FloatArray, srcRate: Int, dstRate: Int): FloatArray {
    if (input.isEmpty()) return input
    val ratio = dstRate.toDouble() / srcRate.toDouble()
    val outLen = kotlin.math.max(1, (input.size * ratio).toInt())
    val out = FloatArray(outLen)
    for (i in 0 until outLen) {
        val srcPos = i / ratio
        val i0 = srcPos.toInt().coerceIn(0, input.lastIndex)
        val i1 = (i0 + 1).coerceAtMost(input.lastIndex)
        val t = (srcPos - i0)
        out[i] = (input[i0] * (1 - t) + input[i1] * t).toFloat()
    }
    return out
}