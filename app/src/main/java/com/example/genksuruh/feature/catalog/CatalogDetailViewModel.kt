package com.example.genksuruh.feature.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Halaman detail kosong sebelum data datang, jadi pakai sealed interface
sealed interface CatalogDetailUiState {
    object Loading : CatalogDetailUiState
    data class Success(val service: Service, val inCart: Boolean) : CatalogDetailUiState
    object NotFound : CatalogDetailUiState
}

// @JvmOverloads: supaya factory bawaan viewModel() tetap menemukan konstruktor (SavedStateHandle)
class CatalogDetailViewModel @JvmOverloads constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CatalogRepository = CatalogRepository
) : ViewModel() {

    private val serviceId: String = checkNotNull(savedStateHandle["serviceId"])

    private val inCart = MutableStateFlow(false)

    val uiState: StateFlow<CatalogDetailUiState> = combine(
        repository.services, inCart
    ) { list, cart ->
        val service = list.find { it.id == serviceId }
        if (service != null) CatalogDetailUiState.Success(service, cart)
        else CatalogDetailUiState.NotFound
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogDetailUiState.Loading
    )

    fun toggleFavorite() {
        viewModelScope.launch { repository.toggleFavorite(serviceId) }
    }

    // Progress 1: hanya penanda. Nanti dihubungkan ke ViewModel keranjang (Rasyid).
    fun addToCart() {
        inCart.value = true
    }
}