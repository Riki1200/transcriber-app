package com.romeodev.core.ui.utils.providers

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.ProvidedValue

actual fun provideNullAndroidOverscrollConfiguration(): Array<ProvidedValue<*>> = arrayOf(
    LocalOverscrollFactory provides null
)