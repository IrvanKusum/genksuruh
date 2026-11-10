package com.example.genksuruh.feature.promo

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.genksuruh.R

object PromoColors {
    val Primary = Color(0xFF0077B6)
    val PrimaryDark = Color(0xFF023E8A)
    val Coral = Color(0xFFFF7A59)
    val Sand = Color(0xFFFFC857)
    val Background = Color(0xFFF4FAFD)
    val SoftBlue = Color(0xFFE3F2FA)
    val Aktif = Color(0xFF2E8B6E)
    val Terpakai = Color(0xFF8A99A3)
    val Kedaluwarsa = Color(0xFFD64545)
    val TextDark = Color(0xFF12263A)
    val TextMuted = Color(0xFF6B7C8C)
    val Outline = Color(0xFFD5E1E8)
}

/** File font harus ada di res/font: sora_regular, sora_medium, sora_semibold, sora_bold (.ttf) */
val PromoFont = FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_medium, FontWeight.Medium),
    Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold)
)

private fun Typography.withFont(f: FontFamily) = copy(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f)
)

@Composable
fun PromoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PromoColors.Primary,
            onPrimary = Color.White,
            primaryContainer = PromoColors.SoftBlue,
            onPrimaryContainer = PromoColors.PrimaryDark,
            secondary = PromoColors.Coral,
            secondaryContainer = PromoColors.SoftBlue,
            onSecondaryContainer = PromoColors.PrimaryDark,
            background = PromoColors.Background,
            onBackground = PromoColors.TextDark,
            surface = Color.White,
            onSurface = PromoColors.TextDark,
            surfaceVariant = PromoColors.SoftBlue,
            onSurfaceVariant = PromoColors.TextMuted,
            surfaceTint = Color.White,
            surfaceContainerLow = Color.White,
            surfaceContainerHigh = Color.White,
            outline = PromoColors.Outline,
            error = PromoColors.Kedaluwarsa
        ),
        typography = Typography().withFont(PromoFont)
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = PromoFont)
        ) {
            content()
        }
    }
}