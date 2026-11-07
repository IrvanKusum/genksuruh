package com.example.genksuruh.core.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

// MODEL (Repository): satu-satunya pintu akun & sesi.
// Progress 2: login/logout/updateProfile diganti Supabase Auth + tabel profiles.
object AuthRepository {

    const val DEMO_ADMIN_EMAIL = "admin@genksuruh.co"
    const val DEMO_ADMIN_PASSWORD = "admin123"
    const val DEMO_USER_EMAIL = "user@genksuruh.co"
    const val DEMO_USER_PASSWORD = "user123"

    // id admin sama dengan CatalogRepository.CURRENT_USER_ID
    private var accounts = listOf(
        Account("usr-irvan", "Irvan Nabil", DEMO_ADMIN_EMAIL, "081234567890", AccountRole.ADMIN),
        Account("usr-mahasiswa", "Mahasiswa Demo", DEMO_USER_EMAIL, "085712345678", AccountRole.USER)
    )

    private val passwords = mapOf(
        DEMO_ADMIN_EMAIL to DEMO_ADMIN_PASSWORD,
        DEMO_USER_EMAIL to DEMO_USER_PASSWORD
    )

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    suspend fun login(email: String, password: String) {
        delay(900) // anggap waktu tunggu jaringan
        val key = email.trim().lowercase()
        val found = accounts.firstOrNull { it.email == key }
        if (found == null || passwords[key] != password) {
            throw IllegalArgumentException("Email atau kata sandi salah")
        }
        _account.value = found
    }

    fun logout() {
        _account.value = null
    }

    // Nama yang mengandung "gagal" ditolak, untuk menguji state error
    suspend fun updateProfile(name: String, phone: String, photoUri: String?) {
        delay(700)
        val current = _account.value ?: throw IllegalStateException("Belum login")
        if (name.contains("gagal", ignoreCase = true)) {
            throw IOException("Server menolak perubahan profil (simulasi)")
        }
        val updated = current.copy(name = name.trim(), phone = phone, photoUri = photoUri)
        accounts = accounts.map { if (it.id == updated.id) updated else it }
        _account.value = updated
    }
}