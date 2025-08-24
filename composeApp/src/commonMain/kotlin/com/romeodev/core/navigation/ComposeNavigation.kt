package com.romeodev.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.romeodev.core.events.navigator.interfaces.Navigator
import com.romeodev.core.events.navigator.utils.handleNavigationAction
import com.romeodev.core.events.utils.ObserveAsEvents
import com.romeodev.core.navigation.nav_graphs.appNavGraph
import com.romeodev.core.navigation.screens.StarterScreens
import com.romeodev.core.ui.composition_locals.LocalNavController
import org.koin.compose.koinInject


@Composable
fun ComposeNavigation(
    scaffoldModifier: Modifier = Modifier,
    navigator: Navigator = koinInject(),
    navController: NavHostController = rememberNavController(),
) {
    NavigationSideEffects(navigator, navController)

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = StarterScreens.Root
        ) {

            appNavGraph(
                scaffoldModifier = scaffoldModifier
            )

        }
    }
}

@Composable
private fun NavigationSideEffects(
    navigator: Navigator,
    navController: NavHostController,
) {
    ObserveAsEvents(
        flow = navigator.navigationActions
    ) { action ->
        navController.handleNavigationAction(
            action = action
        )
    }
}