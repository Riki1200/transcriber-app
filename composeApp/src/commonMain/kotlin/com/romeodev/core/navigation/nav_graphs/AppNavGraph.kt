package com.romeodev.core.navigation.nav_graphs

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import com.romeodev.core.purchases.presentation.ui_main.navigation.purchasesNavGraph
import com.romeodev.features.auth.presentation.ui_main.navigation.authNavGraph

fun NavGraphBuilder.appNavGraph(
    scaffoldModifier: Modifier,
) {
    authNavGraph(
        scaffoldModifier = scaffoldModifier
    )
    starterNavGraph(
        scaffoldModifier = scaffoldModifier
    )
    purchasesNavGraph(
        scaffoldModifier = scaffoldModifier
    )
    /*Todo add other nav graphs here*/

    transcriberNavGraph(
        scaffoldModifier = scaffoldModifier
    )

}
