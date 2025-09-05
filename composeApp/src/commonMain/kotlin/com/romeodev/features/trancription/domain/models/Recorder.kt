package com.romeodev.features.trancription.domain.models

interface Recorder {

    suspend fun start(outputPath: String? = null)

    suspend fun stop(): Recorded
}

data class Recorded(val wavPath: String)