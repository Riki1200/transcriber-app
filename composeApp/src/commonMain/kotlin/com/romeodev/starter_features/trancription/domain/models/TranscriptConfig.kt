package com.romeodev.starter_features.trancription.domain.models

data class TranscriptConfig(
    val languageHint: String? = null,
    val enableTimestamps: Boolean = false
)