package com.example.genksuruh.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    background = ScreenBg,
    surface = Color.White,
    onSurface = TextDark,
    onBackground = TextDark,
    error = DangerRed,
)

@Composable
fun GenkSuruhTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}