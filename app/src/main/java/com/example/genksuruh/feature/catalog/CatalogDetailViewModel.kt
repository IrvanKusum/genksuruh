package com.example.genksuruh.feature.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CatalogDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val serviceId: String = checkNotNull(savedStateHandle["serviceId"])

    private val _service = MutableStateFlow<Service?>(
        DummyServices.list.find { it.id == serviceId }
    )
    val service: StateFlow<Service?> = _service.asStateFlow()

    private val _inCart = MutableStateFlow(false)
    val inCart: StateFlow<Boolean> = _inCart.asStateFlow()

    fun toggleFavorite() {
        _service.update { it?.copy(isFavorite = !it.isFavorite) }
    }

    // Progress 1: hanya penanda. Nanti dihubungkan ke ViewModel keranjang (Rasyid).
    fun addToCart() {
        _inCart.value = true
    }
}