package com.romeodev.starter_features.trancription.core

import com.romeodev.starter_features.trancription.domain.models.TranscriptChunk
import com.romeodev.starter_features.trancription.domain.models.TranscriptConfig
import com.romeodev.starter_features.trancription.domain.models.TranscriptResult
import kotlinx.coroutines.flow.Flow


sealed class TranscriptSource {
    data class Url(val value: String): TranscriptSource()
    data class Path(val value: String): TranscriptSource()
    data class Bytes(val data: ByteArray, val mimeType: String = "audio/wav"): TranscriptSource() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Bytes

            if (!data.contentEquals(other.data)) return false
            if (mimeType != other.mimeType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }
}


expect class TranscriptEngine(config: TranscriptConfig = TranscriptConfig()) {
    /** Transcribe a full file (audio or video). */
    suspend fun transcribeFile(source: TranscriptSource): TranscriptResult
    /** Optional stream API for live mic or chunked uploads. */
    fun stream(): Flow<TranscriptChunk>
}