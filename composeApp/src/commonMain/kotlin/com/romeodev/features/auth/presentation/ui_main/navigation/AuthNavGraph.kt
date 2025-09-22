package com.romeodev.features.auth.presentation.ui_main.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.romeodev.core.ui.utils.navigation.appNavComposable
import com.romeodev.features.auth.presentation.ui_main.screens.SignInScreen
import com.romeodev.features.auth.presentation.ui_main.screens.SignUpScreen
import com.romeodev.features.auth.presentation.ui_main.screens.StartScreen

fun NavGraphBuilder.authNavGraph(
    scaffoldModifier: Modifier,
) {

    navigation<AuthScreens.Root>(
        startDestination = AuthScreens.StartScreen,
    ) {
        appNavComposable<AuthScreens.StartScreen> {
            StartScreen(
                modifier = scaffoldModifier,
            )
        }

        appNavComposable<AuthScreens.SignIn> {
            SignInScreen(
                modifier = scaffoldModifier,
            )
        }

        appNavComposable<AuthScreens.SignUp> {
            SignUpScreen(
                modifier = scaffoldModifier,
            )
        }

    }
}