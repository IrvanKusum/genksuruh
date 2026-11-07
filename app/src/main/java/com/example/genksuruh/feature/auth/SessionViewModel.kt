package com.example.genksuruh.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.auth.Account
import com.example.genksuruh.core.auth.AccountRole
import com.example.genksuruh.core.auth.AuthRepository
import com.example.genksuruh.feature.tracking.TrackingSession
import com.example.genksuruh.feature.tracking.UserRole
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Menyimpan siapa yang sedang login untuk seluruh aplikasi
class SessionViewModel(
    private val repository: AuthRepository = AuthRepository
) : ViewModel() {

    val account: StateFlow<Account?> = repository.account

    init {
        // Samakan peran fitur Riwayat (milik Hilmiy) dengan peran login
        viewModelScope.launch {
            repository.account.collect { acc ->
                TrackingSession.role =
                    if (acc?.role == AccountRole.ADMIN) UserRole.PROVIDER else UserRole.USER
            }
        }
    }
}