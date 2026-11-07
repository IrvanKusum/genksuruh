package com.example.genksuruh.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.auth.AccountRole
import com.example.genksuruh.core.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

data class LoginUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository
) : ViewModel() {

    // Teks input -> mutableStateOf (kursor aman)
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = null, errorMessage = null) }
    }

    // Pintasan untuk demo/dosen
    fun fillDemo(role: AccountRole) {
        if (role == AccountRole.ADMIN) {
            email = AuthRepository.DEMO_ADMIN_EMAIL
            password = AuthRepository.DEMO_ADMIN_PASSWORD
        } else {
            email = AuthRepository.DEMO_USER_EMAIL
            password = AuthRepository.DEMO_USER_PASSWORD
        }
        _uiState.update { LoginUiState() }
    }

    fun login() {
        if (_uiState.value.isLoading) return

        val emailError = when {
            email.isBlank() -> "Email wajib diisi"
            !EMAIL_REGEX.matches(email.trim()) -> "Format email tidak valid"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Kata sandi wajib diisi"
            password.length < 6 -> "Minimal 6 karakter"
            else -> null
        }
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.login(email, password)
                password = "" // bersihkan, layar login akan diganti otomatis
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal masuk") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}