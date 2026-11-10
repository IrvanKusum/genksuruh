package com.example.genksuruh.feature.favorite.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

fun rupiah(value: Int): String =
    "Rp " + value.toString().reversed().chunked(3).joinToString(".").reversed()

fun ratingText(value: Double): String =
    String.format(Locale.US, "%.1f", value).replace('.', ',')

data class CategoryStyle(
    val background: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val gradient: List<Color>
)

fun categoryStyle(category: String): CategoryStyle = when (category) {
    "Antar Makanan" -> CategoryStyle(
        background = Color(0xFFFFE9DA),
        iconTint = Color(0xFFE8731F),
        icon = Icons.Rounded.Fastfood,
        gradient = listOf(Color(0xFFFF9500), Color(0xFFE05A00))
    )
    "Beli Jajan" -> CategoryStyle(
        background = Color(0xFFFFF1C7),
        iconTint = Color(0xFFF5A524),
        icon = Icons.Rounded.LocalCafe,
        gradient = listOf(Color(0xFFFFB627), Color(0xFFE08A00))
    )
    "Nonton Konser" -> CategoryStyle(
        background = Color(0xFFDDF0FF),
        iconTint = Color(0xFF0077B6),
        icon = Icons.Rounded.ConfirmationNumber,
        gradient = listOf(Color(0xFF2E9BDA), Color(0xFF023E8A))
    )
    "Ganti Gas Elpiji" -> CategoryStyle(
        background = Color(0xFFD9F7E4),
        iconTint = Color(0xFF2E9E6B),
        icon = Icons.Rounded.LocalFireDepartment,
        gradient = listOf(Color(0xFF3CB98A), Color(0xFF1B7A55))
    )
    "Fotokopi" -> CategoryStyle(
        background = Color(0xFFE3E4FF),
        iconTint = Color(0xFF4A3BC9),
        icon = Icons.Rounded.Print,
        gradient = listOf(Color(0xFF6B5CE0), Color(0xFF3B2FA8))
    )
    "Bersih Kos" -> CategoryStyle(
        background = Color(0xFFD7F9F1),
        iconTint = Color(0xFF14B8A6),
        icon = Icons.Rounded.CleaningServices,
        gradient = listOf(Color(0xFF2DD4BF), Color(0xFF0F766E))
    )
    "Antar Jemput" -> CategoryStyle(
        background = Color(0xFFE0F2FE),
        iconTint = Color(0xFF0284C7),
        icon = Icons.Rounded.TwoWheeler,
        gradient = listOf(Color(0xFF38BDF8), Color(0xFF0369A1))
    )
    "Tugas Kuliah" -> CategoryStyle(
        background = Color(0xFFF3E8FF),
        iconTint = Color(0xFF9333EA),
        icon = Icons.Rounded.Description,
        gradient = listOf(Color(0xFFA855F7), Color(0xFF6B21A8))
    )
    "Laundry" -> CategoryStyle(
        background = Color(0xFFCFF6FA),
        iconTint = Color(0xFF0A8FA6),
        icon = Icons.Rounded.LocalLaundryService,
        gradient = listOf(Color(0xFF2CC3DB), Color(0xFF0A7C93))
    )
    else -> CategoryStyle(
        background = Color(0xFFCFF6FA),
        iconTint = Color(0xFF0A8FA6),
        icon = Icons.Rounded.LocalLaundryService,
        gradient = listOf(Color(0xFF2CC3DB), Color(0xFF0A7C93))
    )
}
