package com.romeodev.starter_features.trancription.core

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.romeodev.starter_features.trancription.core.error.TranscriptException
import com.romeodev.starter_features.trancription.domain.models.TranscriptChunk
import com.romeodev.starter_features.trancription.domain.models.TranscriptConfig
import com.romeodev.starter_features.trancription.domain.models.TranscriptResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.*
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidTranscriptDeps(
    val appContext: Context,
    val backendBaseUrl: String,
    val apiKey: String? = null
)

@Serializable
private data class TranscribeResponse(
    val text: String,
    val language: String? = null
)

actual class TranscriptEngine actual constructor(
    private val config: TranscriptConfig
) {
    companion object { lateinit var deps: AndroidTranscriptDeps }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json() }
    }

    actual fun stream(): Flow<TranscriptChunk> = emptyFlow()

    actual suspend fun transcribeFile(source: TranscriptSource): TranscriptResult =
        withContext(Dispatchers.IO) {
            val ctx = deps.appContext
            val inputFile = when (source) {
                is TranscriptSource.Path -> File(source.value)
                is TranscriptSource.Url -> FileExtractor.downloadToCache(ctx, source.value)
                is TranscriptSource.Bytes -> FileExtractor.bytesToTemp(ctx, source.data, source.mimeType)
            }


            val m4aFile = AudioExtract.extractToM4a(ctx, inputFile)

            val result = uploadToBackend(m4aFile)
            TranscriptResult(fullText = result.text, languageCode = result.language)
        }

    @OptIn(InternalAPI::class)
    private suspend fun uploadToBackend(audioFile: File): TranscribeResponse {
        val url = "${TranscriptEngine.deps.backendBaseUrl}/transcribe"
        val response: HttpResponse = client.submitFormWithBinaryData(url, formData {
            append("file", audioFile.readBytes(), Headers.build {
                // Whisper/otros aceptan audio/mp4 para .m4a
                append(io.ktor.http.HttpHeaders.ContentType, ContentType.parse("audio/mp4"))
                append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=\"audio.m4a\"")
            })
            config.languageHint?.let { append("language", it) }
            append("timestamps", config.enableTimestamps.toString())
        }) {
            TranscriptEngine.deps.apiKey?.let { header("Authorization", "Bearer $it") }
        }
        if (!response.status.isSuccess()) throw TranscriptException("Backend error: ${response.status}") as Throwable
        return response.body()
    }
}

// Helpers (Android)
private object FileExtractor {
    fun downloadToCache(ctx: Context, url: String): File {
        val tmp = File(ctx.cacheDir, "dl-${UUID.randomUUID()}")
        java.net.URL(url).openStream().use { input -> tmp.outputStream().use { input.copyTo(it) } }
        return tmp
    }
    fun bytesToTemp(ctx: Context, data: ByteArray, mime: String): File {
        val ext = when (mime) {
            "audio/wav" -> ".wav"
            "audio/mpeg", "audio/mp3" -> ".mp3"
            "video/mp4" -> ".mp4"
            else -> ".bin"
        }
        val f = File(ctx.cacheDir, "bytes-${UUID.randomUUID()}$ext")
        f.writeBytes(data)
        return f
    }
}

/**
 * Extrae solo audio (AAC) a .m4a desde un File (video o audio) usando Media3 Transformer.
 * Es asíncrono; se suspende hasta terminar (o fallar).
 */
private object AudioExtract {
    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun extractToM4a(ctx: Context, input: File): File = withContext(Dispatchers.Default) {
        val out = File(ctx.cacheDir, "aud-${UUID.randomUUID()}.m4a")
        val mediaItem = MediaItem.fromUri(Uri.fromFile(input))

        val edited = EditedMediaItem.Builder(mediaItem)
            .setRemoveVideo(true) // quita el video -> deja solo audio
            .build()

        val transformer = Transformer.Builder(ctx)
            .setAudioMimeType(MimeTypes.AUDIO_AAC) // AAC-LC (compatibilidad alta)
            .build()

        suspendCancellableCoroutine<File> { cont ->
            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (!cont.isCompleted) cont.resume(out)
                }
                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exception: ExportException
                ) {
                    if (!cont.isCompleted) cont.resumeWithException(
                        TranscriptException("Media3 transform failed", exception)
                    )
                }
            })
            transformer.start(edited, out.absolutePath)
            cont.invokeOnCancellation { transformer.cancel() }
        }
    }
}
