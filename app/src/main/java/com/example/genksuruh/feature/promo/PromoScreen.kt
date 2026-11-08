package com.example.genksuruh.feature.promo

import androidx.compose.runtime.Composable

/**
 * Pintu masuk fitur Promo, dipanggil dari navigasi tim.
 * [onOpenProfile] diisi dengan navigasi ke layar profil, contoh:
 *     PromoScreen(onOpenProfile = { navController.navigate("profile") })
 */
@Composable
fun PromoScreen(onOpenProfile: () -> Unit = {}) {
    PromoNavHost(onOpenProfile = onOpenProfile)
}