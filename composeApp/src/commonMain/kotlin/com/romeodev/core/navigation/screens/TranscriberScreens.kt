package com.romeodev.core.navigation.screens

import kotlinx.serialization.Serializable


@Serializable
sealed class TranscriberScreens {

    @Serializable
    data object Root : TranscriberScreens()

    @Serializable
    data object MainScreen : TranscriberScreens()
}