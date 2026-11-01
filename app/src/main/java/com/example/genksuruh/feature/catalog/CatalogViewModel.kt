package com.example.genksuruh.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PriceFilter(val label: String) {
    ALL("Semua harga"),
    UNDER_10K("< 10rb"),
    RANGE_10_50K("10rb - 50rb"),
    OVER_50K("> 50rb")
}

data class CatalogUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategory: String = "Semua",
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val services: List<Service> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && services.isEmpty()
}

class CatalogViewModel : ViewModel() {

    private val allServices = MutableStateFlow<List<Service>>(emptyList())
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow("Semua")
    private val priceFilter = MutableStateFlow(PriceFilter.ALL)
    private val loading = MutableStateFlow(true)

    val uiState: StateFlow<CatalogUiState> = combine(
        allServices, query, category, priceFilter, loading
    ) { all, q, c, p, isLoading ->
        val filtered = all.filter { s ->
            val matchQuery = q.isBlank() ||
                    s.title.contains(q, ignoreCase = true) ||
                    s.providerName.contains(q, ignoreCase = true)
            val matchCategory = c == "Semua" || s.category == c
            val matchPrice = when (p) {
                PriceFilter.ALL -> true
                PriceFilter.UNDER_10K -> s.price < 10_000
                PriceFilter.RANGE_10_50K -> s.price in 10_000..50_000
                PriceFilter.OVER_50K -> s.price > 50_000
            }
            matchQuery && matchCategory && matchPrice
        }
        CatalogUiState(
            isLoading = isLoading,
            query = q,
            selectedCategory = c,
            priceFilter = p,
            services = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    init {
        loadServices()
    }

    // Progress 1: dummy. Progress 2: ganti dengan pemanggilan Supabase.
    private fun loadServices() {
        viewModelScope.launch {
            loading.value = true
            delay(800) // simulasi loading supaya skeleton terlihat
            allServices.value = DummyServices.list
            loading.value = false
        }
    }

    fun onQueryChange(value: String) { query.value = value }

    fun onCategorySelected(value: String) { category.value = value }

    fun onPriceSelected(value: PriceFilter) {
        // ketuk filter yang sama lagi = batalkan
        priceFilter.value = if (priceFilter.value == value) PriceFilter.ALL else value
    }

    fun resetFilters() {
        query.value = ""
        category.value = "Semua"
        priceFilter.value = PriceFilter.ALL
    }

    fun toggleFavorite(id: String) {
        allServices.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }
}