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



val lightScheme = m3LightColorScheme(
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

val darkScheme = m3DarkColorScheme(
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
    surfaceContainerLowest = Color(0xFF0F0D11),
    surfaceContainerLow = Color(0xFF1C1B1F),
    surfaceContainer = Color(0xFF201F23),
    surfaceContainerHigh = Color(0xFF2B2A2E),
    surfaceContainerHighest = Color(0xFF363438),
    surfaceTint =  Color(0xFF363438),
)

// Definición de colores de Material para la app
val appLightPrimary = Color(0xFFADD8E6)
val appLightSecondary = Color(0xFFB2DFDB)
val appLightBackground = Color(0xFFFFFBFE)
val appLightSurface = Color(0xFFF2F2F7)
val appLightOnPrimary = Color(0xFF1E1E1E)
val appLightOnSecondary = Color(0xFF1E1E1E)
val appLightOnBackground = Color(0xFF1C1B1F)
val appLightOnSurface = Color(0xFF1C1B1F)

val appDarkPrimary = Color(0xFF87BFFF)
val appDarkSecondary = Color(0xFF80CBC4)
val appDarkBackground = Color(0xFF121212)
val appDarkSurface = Color(0xFF1E1E1E)
val appDarkOnPrimary = Color(0xFFEFEFEF)
val appDarkOnSecondary = Color(0xFFE0E0E0)
val appDarkOnBackground = Color(0xFFE6E1E5)
val appDarkOnSurface = Color(0xFFE6E1E5)

val AppLightMaterialScheme = m3LightColorScheme(
    primary = appLightPrimary,
    secondary = appLightSecondary,
    background = appLightBackground,
    surface = appLightSurface,
    onPrimary = appLightOnPrimary,
    onSecondary = appLightOnSecondary,
    onBackground = appLightOnBackground,
    onSurface = appLightOnSurface
)

val AppDarkMaterialScheme = m3DarkColorScheme(
    primary = appDarkPrimary,
    secondary = appDarkSecondary,
    background = appDarkBackground,
    surface = appDarkSurface,
    onPrimary = appDarkOnPrimary,
    onSecondary = appDarkOnSecondary,
    onBackground = appDarkOnBackground,
    onSurface = appDarkOnSurface
)

// Definición de colores de Cupertino para la app
val cupertinoLightAccent = Color(0xFFADD8E6)
val cupertinoLightLabel = Color(0xFF2D2D2D)
val cupertinoLightSecondaryLabel = Color(0xFF6D6D6D)
val cupertinoLightTertiaryLabel = Color(0xFFA0A0A0)
val cupertinoLightQuaternaryLabel = Color(0xFFC0C0C0)
val cupertinoLightSystemFill = Color(0xFFD0F0FD)
val cupertinoLightSecondarySystemFill = Color(0xFFFDE2E4)
val cupertinoLightTertiarySystemFill = Color(0xFFFFF5E5)
val cupertinoLightQuaternarySystemFill = Color(0xFFE5F4EA)
val cupertinoLightPlaceholderText = Color(0xFFB0B0B0)
val cupertinoLightSeparator = Color(0xFFE0E0E0)
val cupertinoLightOpaqueSeparator = Color(0xFFCCCCCC)
val cupertinoLightLink = Color(0xFF9D9DED)
val cupertinoLightSystemGroupedBackground = Color(0xFFF7F7FA)
val cupertinoLightSecondarySystemGroupedBackground = Color(0xFFF2F2F5)
val cupertinoLightTertiarySystemGroupedBackground = Color(0xFFEBEBF0)
val cupertinoLightSystemBackground = Color(0xFFFFFFFF)
val cupertinoLightSecondarySystemBackground = Color(0xFFF9F9F9)
val cupertinoLightTertiarySystemBackground = Color(0xFFF2F2F2)

val cupertinoDarkAccent = Color(0xFF87BFFF)
val cupertinoDarkLabel = Color(0xFFEDEDED)
val cupertinoDarkSecondaryLabel = Color(0xFFBBBBBB)
val cupertinoDarkTertiaryLabel = Color(0xFF999999)
val cupertinoDarkQuaternaryLabel = Color(0xFF777777)
val cupertinoDarkSystemFill = Color(0xFF1E3A50)
val cupertinoDarkSecondarySystemFill = Color(0xFF4E3A50)
val cupertinoDarkTertiarySystemFill = Color(0xFF5A3E2B)
val cupertinoDarkQuaternarySystemFill = Color(0xFF3B4E41)
val cupertinoDarkPlaceholderText = Color(0xFF888888)
val cupertinoDarkSeparator = Color(0xFF444444)
val cupertinoDarkOpaqueSeparator = Color(0xFF333333)
val cupertinoDarkLink = Color(0xFFB0A7F3)
val cupertinoDarkSystemGroupedBackground = Color(0xFF121212)
val cupertinoDarkSecondarySystemGroupedBackground = Color(0xFF1C1C1E)
val cupertinoDarkTertiarySystemGroupedBackground = Color(0xFF2A2A2E)
val cupertinoDarkSystemBackground = Color(0xFF000000)
val cupertinoDarkSecondarySystemBackground = Color(0xFF111111)
val cupertinoDarkTertiarySystemBackground = Color(0xFF1A1A1A)


val AppLightCupertinoScheme = cupertinoLightColorScheme(
    accent = cupertinoLightAccent,
    label = cupertinoLightLabel,
    secondaryLabel = cupertinoLightSecondaryLabel,
    tertiaryLabel = cupertinoLightTertiaryLabel,
    quaternaryLabel = cupertinoLightQuaternaryLabel,
    systemFill = cupertinoLightSystemFill,
    secondarySystemFill = cupertinoLightSecondarySystemFill,
    tertiarySystemFill = cupertinoLightTertiarySystemFill,
    quaternarySystemFill = cupertinoLightQuaternarySystemFill,
    placeholderText = cupertinoLightPlaceholderText,
    separator = cupertinoLightSeparator,
    opaqueSeparator = cupertinoLightOpaqueSeparator,
    link = cupertinoLightLink,
    systemGroupedBackground = cupertinoLightSystemGroupedBackground,
    secondarySystemGroupedBackground = cupertinoLightSecondarySystemGroupedBackground,
    tertiarySystemGroupedBackground = cupertinoLightTertiarySystemGroupedBackground,
    systemBackground = cupertinoLightSystemBackground,
    secondarySystemBackground = cupertinoLightSecondarySystemBackground,
    tertiarySystemBackground = cupertinoLightTertiarySystemBackground,
)
val AppDarkCupertinoScheme = cupertinoDarkColorScheme(
    accent = cupertinoDarkAccent,
    label = cupertinoDarkLabel,
    secondaryLabel = cupertinoDarkSecondaryLabel,
    tertiaryLabel = cupertinoDarkTertiaryLabel,
    quaternaryLabel = cupertinoDarkQuaternaryLabel,
    systemFill = cupertinoDarkSystemFill,
    secondarySystemFill = cupertinoDarkSecondarySystemFill,
    tertiarySystemFill = cupertinoDarkTertiarySystemFill,
    quaternarySystemFill = cupertinoDarkQuaternarySystemFill,
    placeholderText = cupertinoDarkPlaceholderText,
    separator = cupertinoDarkSeparator,
    opaqueSeparator = cupertinoDarkOpaqueSeparator,
    link = cupertinoDarkLink,
    systemGroupedBackground = cupertinoDarkSystemGroupedBackground,
    secondarySystemGroupedBackground = cupertinoDarkSecondarySystemGroupedBackground,
    tertiarySystemGroupedBackground = cupertinoDarkTertiarySystemGroupedBackground,
    systemBackground = cupertinoDarkSystemBackground,
    secondarySystemBackground = cupertinoDarkSecondarySystemBackground,
    tertiarySystemBackground = cupertinoDarkTertiarySystemBackground,
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
