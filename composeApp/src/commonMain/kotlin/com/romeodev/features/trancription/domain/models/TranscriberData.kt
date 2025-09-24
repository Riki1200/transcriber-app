package com.romeodev.features.trancription.domain.models

import com.romeodev.core.utils.common.currentMillis
import kotlinx.serialization.Serializable


@Serializable
data class TranscriberData(
    val title: String?,
    val language: String?,
    val data: String?,
    val createdAt: Long = currentMillis,
    val exported: Boolean = false,
)
