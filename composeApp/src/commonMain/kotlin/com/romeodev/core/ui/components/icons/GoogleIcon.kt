package com.romeodev.core.ui.components.icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import org.jetbrains.compose.resources.painterResource
import transcriberfast.composeapp.generated.resources.Res
import transcriberfast.composeapp.generated.resources.ic_google


@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(
            resource = Res.drawable.ic_google
        ),
        contentDescription = "Google Icon",
        modifier = modifier,
    )
}
