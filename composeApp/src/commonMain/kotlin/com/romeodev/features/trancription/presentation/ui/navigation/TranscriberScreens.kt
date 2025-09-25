package com.romeodev.features.trancription.presentation.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class TranscriberScreens {

    @Serializable
    data object Root : TranscriberScreens()

    @Serializable
    data object MainScreen : TranscriberScreens()


    @Serializable
    data object TranscriberScreen : TranscriberScreens()
}