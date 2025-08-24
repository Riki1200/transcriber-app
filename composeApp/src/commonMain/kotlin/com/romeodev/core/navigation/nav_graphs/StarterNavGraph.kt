package com.romeodev.core.navigation.nav_graphs

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.romeodev.core.navigation.screens.StarterScreens
import com.romeodev.core.purchases.presentation.ui_main.navigation.PurchasesScreens
import com.romeodev.core.ui.composition_locals.LocalNavController
import com.romeodev.core.ui.screens.WelcomeScreen
import com.romeodev.core.ui.utils.navigation.appNavComposable

fun NavGraphBuilder.starterNavGraph(
    scaffoldModifier: Modifier,
) {

    navigation<StarterScreens.Root>(
        startDestination = StarterScreens.WelcomeScreen,
    ) {
        appNavComposable<StarterScreens.WelcomeScreen> {
            val navController = LocalNavController.current
            WelcomeScreen(
                modifier = scaffoldModifier,
                onGetStartedClick = {
                    // Example of navigating with nav controller without global nav events
                    navController.navigate(
                        route = PurchasesScreens.SubscriptionScreen
                    )
                }
            )
        }
    }
}




















