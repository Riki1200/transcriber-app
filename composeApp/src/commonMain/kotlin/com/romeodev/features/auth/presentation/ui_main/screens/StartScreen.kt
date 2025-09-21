package com.romeodev.features.auth.presentation.ui_main.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Light
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.VoiceChat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.romeodev.core.ui.components.ScreenPreview
import com.romeodev.features.auth.presentation.events.AuthEvents
import com.romeodev.features.auth.presentation.ui_main.navigation.AuthScreens
import com.romeodev.features.auth.presentation.viewmodels.AuthViewModel
import com.slapps.cupertino.adaptive.AdaptiveButton
import com.slapps.cupertino.adaptive.AdaptiveScaffold
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import transcriberfast.composeapp.generated.resources.Res
import transcriberfast.composeapp.generated.resources.finish
import transcriberfast.composeapp.generated.resources.next
import transcriberfast.composeapp.generated.resources.*


data class OnboardingPage(
    val imageRes: ImageVector?,
    val iconColor: Color? = null,
    val title: StringResource,
    val subtitle: StringResource,
    val description: StringResource
)

val onboardingPages = listOf(
    OnboardingPage(
        title = Res.string.onboarding_welcome_title,
        imageRes = Icons.Outlined.RecordVoiceOver,
        subtitle = Res.string.onboarding_welcome_subtitle,
        description = Res.string.onboarding_welcome_desc,
        iconColor = Color.Red
    ),
    OnboardingPage(
        title = Res.string.onboarding_fast_title,
        imageRes = Icons.Outlined.ElectricBolt,
        subtitle = Res.string.onboarding_fast_subtitle,
        description = Res.string.onboarding_fast_desc,
    ),
    OnboardingPage(
        title = Res.string.onboarding_features_title,
        imageRes = Icons.Outlined.StarOutline,
        subtitle = Res.string.onboarding_features_subtitle,
        description = Res.string.onboarding_features_desc,
    )
)


@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalAdaptiveApi::class)
fun StartScreen(
    onFinished: () -> Unit = {},
    viewModel: AuthViewModel = koinInject(),
) {
    var currentPage by remember { mutableStateOf(0) }
    val progressList =
        remember { mutableStateListOf<Float>().apply { repeat(onboardingPages.size) { add(0f) } } }

    LaunchedEffect(currentPage, progressList) {
        while (currentPage < onboardingPages.size) {
            progressList[currentPage] = 0f
            while (progressList[currentPage] < 1f) {
                delay(60L)
                progressList[currentPage] = (progressList[currentPage] + 0.01f).coerceAtMost(1f)
                println("progressList: $progressList")
            }
            delay(2000L)
            currentPage++
        }

        // Reinicio
        currentPage = 0
        progressList.fill(0f)
    }


    val page = onboardingPages[currentPage]

    AdaptiveScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                AnimatedContent(

                    targetState = page,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { it } togetherWith fadeOut()
                    },
                    label = "PageTransition"
                ) { animatedPage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {


                        if (animatedPage.imageRes != null)
                            Icon(
                                imageVector = animatedPage.imageRes,
                                tint = animatedPage.iconColor ?: MaterialTheme.colorScheme.primary,
                                contentDescription = "",
                                modifier = Modifier.size(100.dp)
                            )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(animatedPage.title).trim(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(animatedPage.description).trim(),
                            fontSize = 16.sp,
                            modifier = Modifier,
                            color = Color.Gray
                        )
                    }
                }


                LazyRow {
                    items(items = progressList) { it ->
                        Spacer(modifier = Modifier.width(8.dp))

                        LinearProgressIndicator(
                            progress = { it.coerceIn(0f, 1f) },
                            strokeCap = StrokeCap.Square,
                            drawStopIndicator = {},
                            modifier = Modifier
                                .width(10.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.primary,
                        )

                    }
                }


                AdaptiveButton(
                    onClick = {
                        if (currentPage < onboardingPages.lastIndex) {
                            progressList[currentPage] = 1f
                            currentPage++

                        } else {
                            viewModel.onEvent(AuthEvents.Navigate(AuthScreens.SignIn))
                        }
                    },

                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        ).fillMaxWidth()
                ) {
                    Text(
                        (if (currentPage == onboardingPages.lastIndex)
                            stringResource(Res.string.finish)
                        else stringResource(Res.string.next))
                            .trim()
                    )
                }
            }
        }
    }


}

@Preview
@Composable
fun StartScreenPreview() {
    ScreenPreview() {
        StartScreen()
    }

}