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
    val nameError: String? = null,
    val phoneError: String? = null,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository
) : ViewModel() {

    var isRegisterMode by mutableStateOf(false)
        private set

    var name by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        isRegisterMode = !isRegisterMode
        name = ""
        phone = ""
        email = ""
        password = ""
        _uiState.update { LoginUiState() }
    }

    fun onNameChange(value: String) {
        name = value.take(40)
        _uiState.update { it.copy(nameError = null, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        phone = value.filter { it.isDigit() }.take(14)
        _uiState.update { it.copy(phoneError = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        email = value
        _uiState.update { it.copy(emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        password = value
        _uiState.update { it.copy(passwordError = null, errorMessage = null) }
    }

    fun authenticate() {
        if (_uiState.value.isLoading) return

        val nameError = if (isRegisterMode && name.trim().length < 3) "Nama minimal 3 karakter" else null
        val phoneError = if (isRegisterMode && phone.length < 9) "Nomor HP minimal 9 digit" else null
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

        if (emailError != null || passwordError != null || nameError != null || phoneError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    nameError = nameError,
                    phoneError = phoneError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                if (isRegisterMode) {
                    repository.register(name, email, phone, password, AccountRole.USER)
                } else {
                    repository.login(email, password)
                }
                password = ""
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: if (isRegisterMode) "Gagal mendaftar" else "Gagal masuk")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
