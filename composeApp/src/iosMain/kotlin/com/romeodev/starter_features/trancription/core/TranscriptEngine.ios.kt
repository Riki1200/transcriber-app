package com.romeodev.starter_features.trancription.core

import com.romeodev.starter_features.trancription.core.error.TranscriptException
import com.romeodev.starter_features.trancription.domain.models.TranscriptChunk
import com.romeodev.starter_features.trancription.domain.models.TranscriptConfig
import com.romeodev.starter_features.trancription.domain.models.TranscriptResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import kotlin.coroutines.resume
import com.romeodev.transcriberFast.Transcriber.IosTranscriber
import platform.Foundation.NSError
import kotlin.coroutines.resumeWithException


actual class TranscriptEngine actual constructor(
    private val config: TranscriptConfig
) {
    private val impl = IosTranscriber()


     actual fun stream(): Flow<TranscriptChunk> = emptyFlow()


     actual suspend fun transcribeFile(source: TranscriptSource): TranscriptResult {
        val url: NSURL = when (source) {
            is TranscriptSource.Url -> NSURL(string = source.value)
            is TranscriptSource.Path -> NSURL(fileURLWithPath = source.value)
            is TranscriptSource.Bytes -> throw TranscriptException("Use Url/Path on iOS for now")
        }






        val (text, lang) = try {
            val result = suspendCancellableCoroutine<Pair<String,String?>> { cont ->

                impl.transcribeFileWithUrl(
                    url,
                    config.languageHint
                ) { returnedText: String?, returnedLang: String?, error: NSError? ->
                    when {
                        error != null -> if (!cont.isCompleted) {
                            cont.resumeWithException(TranscriptException(error.localizedDescription))
                        }
                        returnedText != null -> if (!cont.isCompleted) {
                            cont.resume(returnedText to returnedLang)
                        }
                        else -> if (!cont.isCompleted) {
                            cont.resume("" to returnedLang)
                        }
                    }
                }

            }
            result
        } catch (t: Throwable) {
            throw TranscriptException("iOS transcribe failed", t)
        }
        return TranscriptResult(fullText = text, languageCode = lang)
    }
}