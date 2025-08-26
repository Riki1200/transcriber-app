package com.romeodev.starter_features.trancription.domain.models

data class TranscriptResult(
    val fullText: String,
    val chunks: List<TranscriptChunk> = emptyList(),
    val languageCode: String? = null
)