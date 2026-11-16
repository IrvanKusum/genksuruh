package com.example.genksuruh.core.auth

import android.util.Log
import com.example.genksuruh.core.network.StorageUploader
import com.example.genksuruh.core.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException

// MODEL (Repository): satu-satunya pintu akun & sesi berbasis Supabase Auth + tabel profiles.
object AuthRepository {

    const val DEMO_ADMIN_EMAIL = "admin@genksuruh.co"
    const val DEMO_ADMIN_PASSWORD = "admin123"
    const val DEMO_USER_EMAIL = "user@genksuruh.co"
    const val DEMO_USER_PASSWORD = "user123"

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    suspend fun login(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        try {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = cleanEmail
                this.password = password
            }
            refreshProfile()
        } catch (e: Exception) {
            // Otomatis daftarkan jika akun demo belum terdaftar di Supabase
            if (isDemoAccount(cleanEmail, password)) {
                try {
                    val isDemoAdmin = cleanEmail == DEMO_ADMIN_EMAIL
                    register(
                        name = if (isDemoAdmin) "Irvan Nabil" else "Mahasiswa Demo",
                        email = cleanEmail,
                        phone = if (isDemoAdmin) "081234567890" else "085712345678",
                        password = password,
                        role = if (isDemoAdmin) AccountRole.ADMIN else AccountRole.USER
                    )
                    return
                } catch (regError: Exception) {
                    throw IllegalArgumentException("Gagal masuk akun demo: ${regError.message}")
                }
            }
            throw IllegalArgumentException(e.message ?: "Email atau kata sandi salah")
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: AccountRole = AccountRole.USER
    ) {
        val cleanEmail = email.trim().lowercase()
        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = cleanEmail
            this.password = password
            data = buildJsonObject {
                put("name", name.trim())
                put("phone", phone.trim())
                put("role", role.name)
            }
        }
        refreshProfile()
    }

    suspend fun logout() {
        try {
            SupabaseClient.client.auth.signOut()
        } catch (_: Exception) {
        } finally {
            _account.value = null
        }
    }

    suspend fun restoreSession() {
        try {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            if (user != null) {
                refreshProfile()
            } else {
                _account.value = null
            }
        } catch (_: Exception) {
            _account.value = null
        }
    }

    suspend fun refreshProfile() {
        val user = SupabaseClient.client.auth.currentUserOrNull() ?: run {
            _account.value = null
            return
        }

        try {
            val profileDto = SupabaseClient.client.postgrest.from("profiles")
                .select {
                    filter {
                        eq("id", user.id)
                    }
                }.decodeSingleOrNull<ProfileDto>()

            if (profileDto != null) {
                _account.value = profileDto.toAccount()
            } else {
                val nameMeta = user.userMetadata?.get("name")?.toString()?.replace("\"", "") ?: user.email?.substringBefore("@") ?: "User"
                val phoneMeta = user.userMetadata?.get("phone")?.toString()?.replace("\"", "") ?: ""
                val roleMeta = user.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "USER"

                _account.value = Account(
                    id = user.id,
                    name = nameMeta,
                    email = user.email ?: "",
                    phone = phoneMeta,
                    role = if (roleMeta.equals("ADMIN", ignoreCase = true)) AccountRole.ADMIN else AccountRole.USER,
                    watupayBalance = 145_500L
                )
            }
        } catch (_: Exception) {
            val nameMeta = user.userMetadata?.get("name")?.toString()?.replace("\"", "") ?: user.email?.substringBefore("@") ?: "User"
            val phoneMeta = user.userMetadata?.get("phone")?.toString()?.replace("\"", "") ?: ""
            val roleMeta = user.userMetadata?.get("role")?.toString()?.replace("\"", "") ?: "USER"

            _account.value = Account(
                id = user.id,
                name = nameMeta,
                email = user.email ?: "",
                phone = phoneMeta,
                role = if (roleMeta.equals("ADMIN", ignoreCase = true)) AccountRole.ADMIN else AccountRole.USER,
                watupayBalance = 145_500L
            )
        }
    }

    suspend fun updateProfile(name: String, phone: String, photoBytes: ByteArray? = null, mimeType: String? = null) {
        val current = _account.value ?: throw IllegalStateException("Belum login")

        if (name.contains("gagal", ignoreCase = true)) {
            throw IOException("Server menolak perubahan profil (simulasi)")
        }

        var finalPhotoUrl = current.photoUri
        if (photoBytes != null) {
            try {
                val ext = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }
                val path = "profiles/${current.id}.$ext"
                finalPhotoUrl = StorageUploader.upload(path, photoBytes, overwrite = true)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Upload foto profil gagal: ${e.message}", e)
            }
        }

        try {
            SupabaseClient.client.postgrest.from("profiles")
                .update(ProfileUpdateDto(name = name.trim(), phone = phone.trim(), photoUrl = finalPhotoUrl)) {
                    filter {
                        eq("id", current.id)
                    }
                }
            refreshProfile()
        } catch (e: Exception) {
            val updated = current.copy(name = name.trim(), phone = phone, photoUri = finalPhotoUrl)
            _account.value = updated
        }
    }

    private fun isDemoAccount(email: String, pass: String): Boolean {
        return (email == DEMO_ADMIN_EMAIL && pass == DEMO_ADMIN_PASSWORD) ||
                (email == DEMO_USER_EMAIL && pass == DEMO_USER_PASSWORD)
    }
}
