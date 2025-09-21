package com.romeodev.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle

import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font

import com.slapps.cupertino.theme.Typography as CupertinoTypography
import com.slapps.cupertino.theme.LocalTypography as LocalCupertinoTypography

import androidx.compose.material3.Typography as M3Typography
import org.jetbrains.compose.resources.Font
import transcriberfast.composeapp.generated.resources.Res
import transcriberfast.composeapp.generated.resources.poppins_bold
import transcriberfast.composeapp.generated.resources.poppins_medium
import transcriberfast.composeapp.generated.resources.poppins_regular
import transcriberfast.composeapp.generated.resources.poppins_thin

@Composable
fun getPoppinsFontFamily() = FontFamily(
    Font(
        resource = Res.font.poppins_thin,
        weight = FontWeight.Thin
    ),
    Font(
        resource = Res.font.poppins_regular,
        weight = FontWeight.Normal
    ),
   /*
    Remove poppins medium & bold because they look ugly on the app
    but you can change them if you want
    Font(
        resource = Res.font.poppins_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resource = Res.font.poppins_bold,
        weight = FontWeight.Bold
    )*/
)




// Default Material 3 typography values
val baselineTypography = Typography()



@Composable
fun rememberPoppinsFontFamily(): FontFamily {
    val thin = Font(Res.font.poppins_thin, weight = FontWeight.Thin, style = FontStyle.Normal)
    val regular = Font(Res.font.poppins_regular, weight = FontWeight.Normal, style = FontStyle.Normal)
    val medium = Font(Res.font.poppins_medium, weight = FontWeight.Medium, style = FontStyle.Normal)
    val bold = Font(Res.font.poppins_bold, weight = FontWeight.Bold, style = FontStyle.Normal)

    return remember(regular, medium, thin, bold) {
        FontFamily(regular, medium, thin, bold)
    }
}


fun buildTypography(fontFamily: FontFamily): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp
    ),

    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),

    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    ),
)

fun m3ToCupertino(m3: M3Typography): CupertinoTypography = CupertinoTypography(
    largeTitle = m3.displayLarge,
    title1     = m3.headlineLarge,
    title2     = m3.headlineMedium,
    title3     = m3.headlineSmall,
    headline   = m3.titleLarge,
    body       = m3.bodyLarge,
    callout    = m3.bodyMedium,
    subhead    = m3.bodySmall,
    footnote   = m3.labelLarge,
    caption1   = m3.labelMedium,
    caption2   = m3.labelSmall,
)