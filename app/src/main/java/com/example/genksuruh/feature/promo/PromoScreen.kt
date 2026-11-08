package com.example.genksuruh.feature.promo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.genksuruh.core.auth.AccountRole
import com.example.genksuruh.core.auth.LocalAccountRole

/**
 * Pintu masuk fitur Promo, dipanggil dari AppNavGraph.
 * Akun ADMIN otomatis masuk mode Penyedia (Promo Saya).
 */
@Composable
fun PromoScreen(onOpenProfile: () -> Unit = {}) {
    val role = LocalAccountRole.current

    LaunchedEffect(role) {
        PromoModule.session.setProviderMode(role == AccountRole.ADMIN)
    }

    PromoNavHost(onOpenProfile = onOpenProfile)
}