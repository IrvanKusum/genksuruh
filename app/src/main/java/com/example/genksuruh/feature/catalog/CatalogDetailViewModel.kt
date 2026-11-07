package com.example.genksuruh.feature.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CatalogDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val serviceId: String = checkNotNull(savedStateHandle["serviceId"])

    val service: StateFlow<Service?> = CatalogRepository.services
        .map { list -> list.find { it.id == serviceId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogRepository.getById(serviceId))

    private val _inCart = MutableStateFlow(false)
    val inCart: StateFlow<Boolean> = _inCart.asStateFlow()

    fun toggleFavorite() = CatalogRepository.toggleFavorite(serviceId)

    // Progress 1: hanya penanda. Nanti dihubungkan ke ViewModel keranjang (Rasyid).
    fun addToCart() { _inCart.value = true }
}