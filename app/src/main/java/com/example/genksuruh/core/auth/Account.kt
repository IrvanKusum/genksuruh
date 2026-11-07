package com.example.genksuruh.core.auth

import androidx.compose.runtime.staticCompositionLocalOf

enum class AccountRole(val label: String) {
    USER("Pengguna"),
    ADMIN("Admin")
}

data class Account(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: AccountRole,
    val photoUri: String? = null
) {
    val initial: String
        get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

// Peran aktif untuk semua layar. Cara pakai di layar mana pun:
//   val role = LocalAccountRole.current
//   if (role == AccountRole.ADMIN) { ...tombol tambah/hapus... }
val LocalAccountRole = staticCompositionLocalOf { AccountRole.USER }