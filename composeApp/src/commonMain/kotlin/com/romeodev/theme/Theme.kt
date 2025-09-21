package com.romeodev.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.slapps.cupertino.InternalCupertinoApi
import com.slapps.cupertino.adaptive.AdaptiveTheme
import com.slapps.cupertino.adaptive.CupertinoThemeSpec
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import com.slapps.cupertino.adaptive.MaterialThemeSpec
import com.slapps.cupertino.adaptive.Shapes
import com.slapps.cupertino.adaptive.toCupertino
import com.slapps.cupertino.adaptive.toMaterial
import com.slapps.cupertino.theme.CupertinoTheme
import com.slapps.cupertino.theme.LocalTypography
import androidx.compose.material3.darkColorScheme as m3DarkColorScheme
import androidx.compose.material3.lightColorScheme as m3LightColorScheme
import com.slapps.cupertino.theme.darkColorScheme as cupertinoDarkColorScheme
import com.slapps.cupertino.theme.lightColorScheme as cupertinoLightColorScheme

val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)


val AppLightMaterialScheme = m3LightColorScheme(
    primary = Color(0xFFADD8E6),
    secondary = Color(0xFFB2DFDB),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFF2F2F7),
    onPrimary = Color(0xFF1E1E1E),
    onSecondary = Color(0xFF1E1E1E),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

val AppDarkMaterialScheme = m3DarkColorScheme(
    primary = Color(0xFF87BFFF),
    secondary = Color(0xFF80CBC4),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFFEFEFEF),
    onSecondary = Color(0xFFE0E0E0),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)


val AppLightCupertinoScheme = cupertinoLightColorScheme(
    accent = Color(0xFFADD8E6),
    label = Color(0xFF2D2D2D),
    secondaryLabel = Color(0xFF6D6D6D),
    tertiaryLabel = Color(0xFFA0A0A0),
    quaternaryLabel = Color(0xFFC0C0C0),
    systemFill = Color(0xFFD0F0FD),
    secondarySystemFill = Color(0xFFFDE2E4),
    tertiarySystemFill = Color(0xFFFFF5E5),
    quaternarySystemFill = Color(0xFFE5F4EA),
    placeholderText = Color(0xFFB0B0B0),
    separator = Color(0xFFE0E0E0),
    opaqueSeparator = Color(0xFFCCCCCC),
    link = Color(0xFF9D9DED),
    systemGroupedBackground = Color(0xFFF7F7FA),
    secondarySystemGroupedBackground = Color(0xFFF2F2F5),
    tertiarySystemGroupedBackground = Color(0xFFEBEBF0),
    systemBackground = Color(0xFFFFFFFF),
    secondarySystemBackground = Color(0xFFF9F9F9),
    tertiarySystemBackground = Color(0xFFF2F2F2),
)
val AppDarkCupertinoScheme = cupertinoDarkColorScheme(
    accent = Color(0xFF87BFFF),
    label = Color(0xFFEDEDED),
    secondaryLabel = Color(0xFFBBBBBB),
    tertiaryLabel = Color(0xFF999999),
    quaternaryLabel = Color(0xFF777777),
    systemFill = Color(0xFF1E3A50),
    secondarySystemFill = Color(0xFF4E3A50),
    tertiarySystemFill = Color(0xFF5A3E2B),
    quaternarySystemFill = Color(0xFF3B4E41),
    placeholderText = Color(0xFF888888),
    separator = Color(0xFF444444),
    opaqueSeparator = Color(0xFF333333),
    link = Color(0xFFB0A7F3),
    systemGroupedBackground = Color(0xFF121212),
    secondarySystemGroupedBackground = Color(0xFF1C1C1E),
    tertiarySystemGroupedBackground = Color(0xFF2A2A2E),
    systemBackground = Color(0xFF000000),
    secondarySystemBackground = Color(0xFF111111),
    tertiarySystemBackground = Color(0xFF1A1A1A),
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
)


@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?


val AdaptiveShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)


@OptIn(ExperimentalAdaptiveApi::class, InternalCupertinoApi::class)
@Composable
fun ApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicColorScheme = getDynamicColorScheme(darkTheme = darkTheme)
    val colorScheme = when {
        dynamicColor && dynamicColorScheme != null -> dynamicColorScheme
        darkTheme -> darkScheme
        else -> lightScheme
    }


    val poppinsFontFamily = rememberPoppinsFontFamily()


    val appTypographyLocal = remember(poppinsFontFamily) { buildTypography(poppinsFontFamily) }

    val cupertinoTypography = remember(appTypographyLocal) { m3ToCupertino(appTypographyLocal) }

    CompositionLocalProvider(LocalTypography provides cupertinoTypography) {
        AdaptiveTheme(
            material = MaterialThemeSpec(
                colorScheme = if (darkTheme) darkScheme
                else lightScheme,
                typography = appTypographyLocal,
                shapes = AdaptiveShapes.toMaterial(),
            ),
            cupertino = CupertinoThemeSpec(
                colorScheme = if (darkTheme) AppDarkCupertinoScheme
                else AppLightCupertinoScheme,
                typography = cupertinoTypography,
                shapes = AdaptiveShapes.toCupertino()
            ),


            ) {
            content()
        }
    }


}
