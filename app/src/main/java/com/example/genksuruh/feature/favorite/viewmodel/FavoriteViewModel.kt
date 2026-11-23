package com.example.genksuruh.feature.favorite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.feature.favorite.data.FavoriteRepository
import com.example.genksuruh.feature.favorite.data.SupabaseFavoriteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FavoriteEvent {
    data class Removed(val item: Service) : FavoriteEvent
    data class Added(val item: Service) : FavoriteEvent
}

/**
 * Favorit terintegrasi dengan Supabase (tabel favorites & services).
 */
class FavoriteViewModel(
    private val repo: FavoriteRepository = FavoriteRepository
) : ViewModel() {

    companion object {
        const val ALL = "Semua"
    }

    private val _favorites = MutableStateFlow<List<Service>>(emptyList())
    val favorites: StateFlow<List<Service>> =
        if (repo is SupabaseFavoriteRepository) {
            repo.favoritesFlow
        } else {
            _favorites.asStateFlow()
        }

    private val _catalog = MutableStateFlow<List<Service>>(emptyList())
    val catalog: StateFlow<List<Service>> = _catalog.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val categories: List<String>
        get() = listOf(ALL) + (repo.getAllCategories() + favorites.value.map { it.category }).distinct()

    val visibleFavorites: StateFlow<List<Service>> =
        combine(favorites, _selectedCategory, _query) { list, category, query ->
            applyFilter(list, category, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _catalogQuery = MutableStateFlow("")
    val catalogQuery: StateFlow<String> = _catalogQuery.asStateFlow()

    private val _selectedCatalogCategory = MutableStateFlow(ALL)
    val selectedCatalogCategory: StateFlow<String> = _selectedCatalogCategory.asStateFlow()

    fun setCatalogQuery(q: String) { _catalogQuery.value = q }
    fun selectCatalogCategory(cat: String) { _selectedCatalogCategory.value = cat }

    val visibleCatalogServices: StateFlow<List<Service>> =
        combine(_catalog, _selectedCatalogCategory, _catalogQuery) { list, category, query ->
            applyFilter(list, category, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<FavoriteEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastRemoved: Service? = null

    init {
        refresh()
    }

    fun refresh() {
        _favorites.value = repo.getFavorites()
        _catalog.value = repo.getAllCatalog()
    }

    fun selectCategory(category: String) { _selectedCategory.value = category }
    fun setQuery(query: String) { _query.value = query }

    fun getById(id: String): Service? = repo.getById(id)

    fun isFavorite(id: String): Boolean = repo.isFavorite(id)

    fun add(item: Service) {
        if (isFavorite(item.id)) return
        val success = repo.add(item.id)
        if (success) {
            refresh()
            _events.trySend(FavoriteEvent.Added(item))
        }
    }

    fun toggleFavorite(item: Service) {
        if (isFavorite(item.id)) remove(item) else add(item)
    }

    fun remove(item: Service) {
        if (!isFavorite(item.id)) return
        lastRemoved = item
        repo.remove(item.id)
        refresh()
        _events.trySend(FavoriteEvent.Removed(item))
    }

    fun undoRemove() {
        val item = lastRemoved ?: return
        lastRemoved = null
        repo.add(item.id)
        refresh()
    }

    fun resetDemo() {
        repo.reset()
        refresh()
    }

    private fun applyFilter(
        list: List<Service>,
        category: String,
        query: String
    ): List<Service> = list.filter { service ->
        (category == ALL || service.category == category) &&
                (query.isBlank() ||
                        service.title.contains(query, ignoreCase = true) ||
                        service.providerName.contains(query, ignoreCase = true))
    }
}
