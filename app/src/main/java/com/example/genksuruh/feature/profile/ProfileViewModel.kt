package com.example.genksuruh.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.auth.Account
import com.example.genksuruh.core.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ===== Layar Profil =====
class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository
) : ViewModel() {

    val account: StateFlow<Account?> = repository.account

    fun logout() = repository.logout()
}

// ===== Layar Edit Profil =====
data class EditProfileUiState(
    val photoUri: String? = null,
    val nameError: String? = null,
    val phoneError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class EditProfileViewModel(
    private val repository: AuthRepository = AuthRepository
) : ViewModel() {

    private val current = repository.account.value

    // Teks yang diproses/divalidasi -> mutableStateOf
    var name by mutableStateOf(current?.name ?: "")
        private set
    var phone by mutableStateOf(current?.phone ?: "")
        private set

    private val _uiState = MutableStateFlow(EditProfileUiState(photoUri = current?.photoUri))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    val initial: String get() = current?.initial ?: "?"

    fun onNameChange(value: String) {
        name = value.take(40)
        _uiState.update { it.copy(nameError = null) }
    }

    fun onPhoneChange(value: String) {
        phone = value.filter { it.isDigit() }.take(14)
        _uiState.update { it.copy(phoneError = null) }
    }

    fun onPhotoPicked(uri: String?) {
        if (uri != null) _uiState.update { it.copy(photoUri = uri) }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photoUri = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun save() {
        if (_uiState.value.isSaving) return

        val nameError = if (name.trim().length < 3) "Nama minimal 3 karakter" else null
        val phoneError = if (phone.length < 9) "Nomor HP minimal 9 digit" else null
        if (nameError != null || phoneError != null) {
            _uiState.update { it.copy(nameError = nameError, phoneError = phoneError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.updateProfile(name, phone, _uiState.value.photoUri)
                _uiState.update { it.copy(saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal menyimpan profil") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}