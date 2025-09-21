package com.romeodev.features.auth.presentation.ui_main.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AuthScreens {
    @Serializable
    data object Root : AuthScreens()




    @Serializable
    data object SignIn : AuthScreens()

    @Serializable
    data object SignUp : AuthScreens()


    @Serializable
    data object StartScreen : AuthScreens()
}
