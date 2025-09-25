package com.romeodev.features.trancription.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect.Companion.cornerPathEffect
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.romeodev.core.ui.components.ScreenPreview
import com.romeodev.features.auth.presentation.ui_main.screens.StartScreen
import com.romeodev.features.trancription.presentation.ui.components.TopAppBarComponent
import com.romeodev.features.trancription.presentation.viewModels.TranscribeViewModel
import com.romeodev.theme.*
import com.romeodev.theme.primaryLight
import com.slapps.cupertino.CupertinoNavigateBackButton
import com.slapps.cupertino.CupertinoText
import com.slapps.cupertino.adaptive.AdaptiveButton
import com.slapps.cupertino.adaptive.AdaptiveScaffold
import com.slapps.cupertino.adaptive.AdaptiveTonalButton
import com.slapps.cupertino.adaptive.AdaptiveWidget
import com.slapps.cupertino.adaptive.CupertinoButtonAdaptation
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.getKoin
import transcriberfast.composeapp.generated.resources.Res
import transcriberfast.composeapp.generated.resources.monthly_usage


@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun HomeScreen(vm: TranscribeViewModel? = getKoin().get()) {

    AdaptiveScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBarComponent() }

    ) { it ->
        Box(
            modifier = Modifier.padding(it).padding(10.dp).fillMaxSize()
        ) {
            CardPlanSelect()
        }

    }
}



@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun CardPlanSelect() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
            .height(160.dp)
            .padding(10.dp)

        ,
        colors = CardDefaults.elevatedCardColors().copy(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),

        ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(tertiaryContainerDark, onPrimaryDark),
                        ), RoundedCornerShape(10)
                    )
                    .padding((1.5).dp),
            )
            Column {
                Row(
                    modifier = Modifier.padding(15.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.monthly_usage),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = scrimLight
                    )
                    Text(
                        text = "45/300 min",
                        style = MaterialTheme.typography.labelMedium,
                        color = scrimLight
                    )
                }
                LinearProgressIndicator(
                    modifier = Modifier.padding(10.dp).fillMaxWidth()

                        .height(15.dp),
                    color = backgroundDark,
                    trackColor = surfaceDimLight,
                    drawStopIndicator = {},
                    strokeCap = StrokeCap.Round.apply {
                        ProgressIndicatorDefaults.LinearStrokeCap
                    },
                    gapSize = (-15).dp,
                    progress = { .5f })



                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "85% remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = onSurfaceLight
                    )

                    Button(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(onTertiaryDark, primaryContainerDark),

                                ), RoundedCornerShape(20)
                            )

                            .height(35.dp),
                        shape = RoundedCornerShape(20),
                        colors = ButtonColors(
                            containerColor = Color.Unspecified,
                            contentColor = Color.Unspecified,
                            disabledContainerColor = Color.Unspecified,
                            disabledContentColor = Color.Unspecified,
                        ),
                        onClick = {},

                        ) {
                        Text(
                            text = "Upgrade to Pro",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,

                            ),

                        )
                    }


                }
            }

        }



    }
}

@Preview
@Composable
fun StartScreenPreview() {
    ScreenPreview() {
        HomeScreen(vm = null)
    }

}