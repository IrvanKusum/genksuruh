package com.example.genksuruh.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.genksuruh.R

val Sora = FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_medium, FontWeight.Medium),
    Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold),
)

// Semua style dipaksa Sora supaya tidak ada teks yang jatuh ke font bawaan
val AppTypography = with(Typography()) {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = Sora),
        displayMedium = displayMedium.copy(fontFamily = Sora),
        displaySmall = displaySmall.copy(fontFamily = Sora),
        headlineLarge = headlineLarge.copy(fontFamily = Sora),
        headlineMedium = headlineMedium.copy(fontFamily = Sora),
        headlineSmall = headlineSmall.copy(fontFamily = Sora),
        titleLarge = titleLarge.copy(fontFamily = Sora),
        titleMedium = titleMedium.copy(fontFamily = Sora, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Sora, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = Sora),
        bodyMedium = bodyMedium.copy(fontFamily = Sora),
        bodySmall = bodySmall.copy(fontFamily = Sora),
        labelLarge = labelLarge.copy(fontFamily = Sora),
        labelMedium = labelMedium.copy(fontFamily = Sora),
        labelSmall = labelSmall.copy(fontFamily = Sora),
    )
}