package com.romeodev.core.navigation.nav_graphs

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.romeodev.core.navigation.screens.StarterScreens
import com.romeodev.core.navigation.screens.TranscriberScreens
import com.romeodev.core.purchases.presentation.ui_main.navigation.PurchasesScreens
import com.romeodev.core.ui.composition_locals.LocalNavController
import com.romeodev.core.ui.screens.WelcomeScreen
import com.romeodev.core.ui.utils.navigation.appNavComposable
import com.romeodev.features.trancription.presentation.ui.TranscribeScreen

fun NavGraphBuilder.transcriberNavGraph(
    scaffoldModifier: Modifier,
) {

    navigation<TranscriberScreens.Root>(
        startDestination = TranscriberScreens.MainScreen,
    ) {
        appNavComposable<TranscriberScreens.MainScreen> {
            val navController = LocalNavController.current
            TranscribeScreen()
        }
    }
}
