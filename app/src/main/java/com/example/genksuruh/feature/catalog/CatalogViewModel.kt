package com.example.genksuruh.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PriceFilter(val label: String) {
    ALL("Semua harga"),
    UNDER_10K("< 10rb"),
    RANGE_10_50K("10rb - 50rb"),
    OVER_50K("> 50rb")
}

data class CatalogUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val query: String = "",
    val selectedCategory: String = "Semua",
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val services: List<Service> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && services.isEmpty()
}

class CatalogViewModel(
    private val repository: CatalogRepository = CatalogRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow("Semua")
    private val priceFilter = MutableStateFlow(PriceFilter.ALL)
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CatalogUiState> = combine(
        repository.services, query, category, priceFilter, loading, error
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val all = values[0] as List<Service>
        val q = values[1] as String
        val c = values[2] as String
        val p = values[3] as PriceFilter
        val isLoading = values[4] as Boolean
        val err = values[5] as String?

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
            errorMessage = err,
            query = q,
            selectedCategory = c,
            priceFilter = p,
            services = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    init {
        loadServices()
    }

    // Ambil data dari Repository (dummy sekarang, Supabase nanti)
    fun loadServices(simulateFailure: Boolean = false) {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                repository.loadServices(simulateFailure)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = e.message ?: "Terjadi kesalahan"
            } finally {
                loading.value = false
            }
        }
    }

    fun onQueryChange(value: String) { query.value = value }

    fun onCategorySelected(value: String) { category.value = value }

    fun onPriceSelected(value: PriceFilter) {
        priceFilter.value = if (priceFilter.value == value) PriceFilter.ALL else value
    }

    fun resetFilters() {
        query.value = ""
        category.value = "Semua"
        priceFilter.value = PriceFilter.ALL
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { repository.toggleFavorite(id) }
    }
}