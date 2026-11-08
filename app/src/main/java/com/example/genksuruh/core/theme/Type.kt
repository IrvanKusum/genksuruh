package com.example.genksuruh.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.genksuruh.R

val Sora = FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_medium, FontWeight.Medium),
    Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold)
)

private fun style(weight: FontWeight, size: Int, line: Int) = TextStyle(
    fontFamily = Sora,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp
)

// HIRARKI TEKS GENKSURUH.CO
//  Judul        headlineMedium 18  SemiBold   judul besar layar, sapaan
//  Subjudul 1   titleLarge     16  SemiBold   judul app bar, judul bagian
//  Subjudul 2   titleMedium    14  SemiBold   judul kartu, nama item, harga
//  Subjudul 3   titleSmall     12  SemiBold   label bagian kecil, judul kartu padat
//  Isi          bodyLarge      13  Normal      paragraf, deskripsi
//               bodyMedium     12  Normal      isi sekunder
//  Keterangan   bodySmall      11  Normal      catatan, info tambahan
//  Tombol/chip  labelLarge     12  Medium      tombol, chip, tab
//               labelMedium    11  Medium      badge, tag
//               labelSmall     10  Medium      kategori, teks atas (huruf kapital)
val AppTypography = Typography(
    headlineLarge = style(FontWeight.Bold, 22, 30),
    headlineMedium = style(FontWeight.SemiBold, 18, 26),
    headlineSmall = style(FontWeight.SemiBold, 16, 24),

    titleLarge = style(FontWeight.SemiBold, 16, 22),
    titleMedium = style(FontWeight.SemiBold, 14, 20),
    titleSmall = style(FontWeight.SemiBold, 12, 18),

    bodyLarge = style(FontWeight.Normal, 13, 20),
    bodyMedium = style(FontWeight.Normal, 12, 18),
    bodySmall = style(FontWeight.Normal, 11, 16),

    labelLarge = style(FontWeight.Medium, 12, 16),
    labelMedium = style(FontWeight.Medium, 11, 14),
    labelSmall = style(FontWeight.Medium, 10, 14)
)