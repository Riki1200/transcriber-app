package com.romeodev.starter_features.trancription.domain.models

data class TranscriptChunk(
    val text: String,
    val startSec: Double? = null,
    val endSec: Double? = null
)