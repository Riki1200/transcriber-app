package com.romeodev.features.trancription.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.romeodev.core.AppConstants
import com.romeodev.core.ui.components.ScreenPreview
import com.romeodev.features.trancription.presentation.ui.screens.HomeScreen
import com.slapps.cupertino.adaptive.AdaptiveTopAppBar
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import com.slapps.cupertino.adaptive.icons.AccountBox
import com.slapps.cupertino.adaptive.icons.AccountCircle
import com.slapps.cupertino.adaptive.icons.AdaptiveIcons
import com.slapps.cupertino.adaptive.icons.Settings
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalAdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    modifier: Modifier = Modifier,
    configAction: () -> Unit = {},
    profileAction: () -> Unit = {},
) {

    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp)
            .background(Color.Gray)
            .shadow(1.dp)
            .zIndex(1f)
        ,

        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = AppConstants.APP_NAME,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = buildAnnotatedString {
                        append("42/")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("120 ")
                        }
                        append("minutes used")

                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }
        },



        actions = {
            IconButton(onClick = configAction) {
                Icon(
                    imageVector = AdaptiveIcons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = profileAction) {
                Icon(
                    imageVector = AdaptiveIcons.Outlined.AccountCircle, contentDescription = "User",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        windowInsets = WindowInsets.statusBars,


        )
}


@Preview
@Composable
fun StartScreenPreview() {
    ScreenPreview() {
        TopAppBarComponent()
    }

}