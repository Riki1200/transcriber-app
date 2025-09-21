package com.romeodev.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.slapps.cupertino.adaptive.AdaptiveTheme
import com.slapps.cupertino.theme.CupertinoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplicationPreview


@Composable
@Preview
fun ScreenPreview(
    content: @Composable () -> Unit
) {

    KoinApplicationPreview(application = { modules() }) {
        AdaptiveTheme(
            material = {
                MaterialTheme(content = it)
            },
            cupertino = {
                CupertinoTheme(content = it)

            },

            content = {
                content()
            })
    }



}