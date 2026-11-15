package com.example.genksuruh.core.auth

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val photoUri: String? = null,
    val watupayBalance: Long = 145_500L
) {
    val initial: String
        get() = name.trim().firstOrNull()?.uppercase() ?: "?"

    val photoUrl: String? get() = photoUri
}

@Serializable
data class ProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: String = "USER",
    @SerialName("watupay_balance")
    val watupayBalance: Long = 145_500L,
    @SerialName("photo_url")
    val photoUrl: String? = null
) {
    fun toAccount(): Account = Account(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = if (role.equals("ADMIN", ignoreCase = true)) AccountRole.ADMIN else AccountRole.USER,
        photoUri = photoUrl,
        watupayBalance = watupayBalance
    )
}

@Serializable
data class ProfileUpdateDto(
    val name: String,
    val phone: String,
    @SerialName("photo_url")
    val photoUrl: String? = null
)

// Peran aktif untuk semua layar. Cara pakai di layar mana pun:
//   val role = LocalAccountRole.current
//   if (role == AccountRole.ADMIN) { ...tombol tambah/hapus... }
val LocalAccountRole = staticCompositionLocalOf { AccountRole.USER }
// Nama akun yang sedang login, untuk sapaan di layar mana pun
val LocalAccountName = staticCompositionLocalOf { "" }
