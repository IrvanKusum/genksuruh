package com.example.genksuruh.feature.favorite.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.genksuruh.R

object FavColors {
    val Primary100 = Color(0xFFCAF0F8)
    val Primary300 = Color(0xFF48CAE4)
    val Primary500 = Color(0xFF0077B6)
    val Primary700 = Color(0xFF023E8A)
    val Primary900 = Color(0xFF03045E)
    val Coral500 = Color(0xFFFF7A59)
    val Sand500 = Color(0xFFFFC857)
    val Danger = Color(0xFFE5484D)
    val Danger100 = Color(0xFFFDE7E8)
    val Bg = Color(0xFFF4FAFD)
    val TextMain = Color(0xFF0B1F33)
    val TextSecondary = Color(0xFF6B8299)
    val Divider = Color(0xFFE3EEF5)
}

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private fun style(
    size: Int,
    weight: FontWeight,
    color: Color = FavColors.TextMain
) = TextStyle(
    fontFamily = Poppins,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp,
    fontWeight = weight,
    color = color
)

object FavType {
    val screenTitle = style(20, FontWeight.SemiBold, FavColors.Primary900)
    val headline20 = style(20, FontWeight.SemiBold)
    val title18 = style(18, FontWeight.SemiBold, FavColors.Primary900)
    val section16 = style(16, FontWeight.SemiBold)
    val title14 = style(14, FontWeight.SemiBold)
    val body14 = style(14, FontWeight.Normal, FavColors.TextSecondary)
    val bodyMain14 = style(14, FontWeight.Normal)
    val mediumMain14 = style(14, FontWeight.Medium)
    val caption12 = style(12, FontWeight.Normal, FavColors.TextSecondary)
    val captionMain12 = style(12, FontWeight.Medium)
    val label11 = style(11, FontWeight.Medium)
    val price14 = style(14, FontWeight.Bold, FavColors.Primary700)
    val price24 = style(24, FontWeight.Bold, FavColors.Primary700)
    val strike14 = style(14, FontWeight.Normal, FavColors.TextSecondary)
        .copy(textDecoration = TextDecoration.LineThrough)
    val chip13 = style(13, FontWeight.Medium)
    val button14 = style(14, FontWeight.SemiBold, Color.White)
}
