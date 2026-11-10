package com.example.genksuruh.feature.favorite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.feature.catalog.CatalogRepository
import com.example.genksuruh.feature.favorite.data.FavoriteService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FavoriteEvent {
    data class Removed(val item: FavoriteService) : FavoriteEvent
    data class Added(val item: FavoriteService) : FavoriteEvent
}

/** Favorit sekarang dibaca dari CatalogRepository (flag isFavorite), satu sumber dengan Beranda. */
class FavoriteViewModel : ViewModel() {

    companion object {
        const val ALL = "Semua"
    }

    private val catalog = CatalogRepository

    init {
        // Jaga-jaga kalau tab Favorit dibuka sebelum Beranda memuat data.
        viewModelScope.launch {
            if (catalog.services.value.isEmpty()) runCatching { catalog.loadServices() }
        }
    }

    val favorites: StateFlow<List<FavoriteService>> = catalog.services
        .map { list -> list.filter { it.isFavorite }.map { it.toFavoriteService() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedCategory = MutableStateFlow(ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val categories: List<String>
        get() = listOf(ALL) + catalog.services.value.map { it.category }.distinct()

    val visibleFavorites: StateFlow<List<FavoriteService>> =
        combine(favorites, _selectedCategory, _query) { list, category, query ->
            applyFilter(list, category, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _catalogQuery = MutableStateFlow("")
    val catalogQuery: StateFlow<String> = _catalogQuery.asStateFlow()

    private val _selectedCatalogCategory = MutableStateFlow(ALL)
    val selectedCatalogCategory: StateFlow<String> = _selectedCatalogCategory.asStateFlow()

    fun setCatalogQuery(q: String) { _catalogQuery.value = q }
    fun selectCatalogCategory(cat: String) { _selectedCatalogCategory.value = cat }

    val visibleCatalogServices: StateFlow<List<FavoriteService>> =
        combine(catalog.services, _selectedCatalogCategory, _catalogQuery) { list, category, query ->
            applyFilter(list.map { it.toFavoriteService() }, category, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<FavoriteEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastRemoved: FavoriteService? = null

    fun selectCategory(category: String) { _selectedCategory.value = category }
    fun setQuery(query: String) { _query.value = query }

    fun getById(id: String): FavoriteService? = catalog.getById(id)?.toFavoriteService()

    fun isFavorite(id: String): Boolean = catalog.getById(id)?.isFavorite == true

    fun add(item: FavoriteService) {
        if (isFavorite(item.id)) return
        viewModelScope.launch {
            catalog.toggleFavorite(item.id)
            _events.trySend(FavoriteEvent.Added(item))
        }
    }

    fun toggleFavorite(item: FavoriteService) {
        if (isFavorite(item.id)) remove(item) else add(item)
    }

    fun remove(item: FavoriteService) {
        if (!isFavorite(item.id)) return
        lastRemoved = item
        viewModelScope.launch {
            catalog.toggleFavorite(item.id)
            _events.trySend(FavoriteEvent.Removed(item))
        }
    }

    fun undoRemove() {
        val item = lastRemoved ?: return
        lastRemoved = null
        viewModelScope.launch {
            if (!isFavorite(item.id)) catalog.toggleFavorite(item.id)
        }
    }

    fun resetDemo() {
        viewModelScope.launch {
            favorites.value.forEach { catalog.toggleFavorite(it.id) }
        }
    }

    private fun applyFilter(
        list: List<FavoriteService>,
        category: String,
        query: String
    ): List<FavoriteService> = list.filter { service ->
        (category == ALL || service.category == category) &&
                (query.isBlank() ||
                        service.title.contains(query, ignoreCase = true) ||
                        service.providerName.contains(query, ignoreCase = true))
    }

    private fun Service.toFavoriteService() = FavoriteService(
        id = id,
        title = title,
        providerName = providerName,
        providerInfo = "Penyedia: $providerName",
        category = category,
        price = price,
        rating = rating,
        reviewCount = reviewCount,
        bannerLabel = title.uppercase(),
        area = areas.joinToString(", ").ifBlank { "Area Kampus UB" },
        estimate = hours.ifBlank { "Sesuai kesepakatan" },
        description = description
    )
}