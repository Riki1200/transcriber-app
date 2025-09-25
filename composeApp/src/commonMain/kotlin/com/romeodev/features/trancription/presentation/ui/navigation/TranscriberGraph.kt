package com.romeodev.features.trancription.presentation.ui.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.romeodev.core.ui.composition_locals.LocalNavController
import com.romeodev.core.ui.utils.navigation.appNavComposable
import com.romeodev.features.trancription.presentation.ui.screens.HomeScreen
import com.romeodev.features.trancription.presentation.ui.screens.TranscribeScreen
import org.koin.compose.getKoin

fun NavGraphBuilder.transcriberNavGraph(
    scaffoldModifier: Modifier,
) {

    navigation<TranscriberScreens.Root>(
        startDestination = TranscriberScreens.MainScreen,
    ) {

        appNavComposable<TranscriberScreens.MainScreen> {
            val navController = LocalNavController.current
            HomeScreen()
        }

        appNavComposable<TranscriberScreens.TranscriberScreen> {
            val navController = LocalNavController.current
            TranscribeScreen(
                vm = getKoin().get()
            )
        }
    }
}
