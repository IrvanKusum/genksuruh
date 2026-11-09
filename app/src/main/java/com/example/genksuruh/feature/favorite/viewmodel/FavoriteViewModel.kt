package com.example.genksuruh.feature.favorite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.feature.favorite.data.FavoriteRepository
import com.example.genksuruh.feature.favorite.data.FavoriteService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

sealed interface FavoriteEvent {
    data class Removed(val item: FavoriteService) : FavoriteEvent
    data class Added(val item: FavoriteService) : FavoriteEvent
}

class FavoriteViewModel : ViewModel() {

    companion object {
        const val ALL = "Semua"
    }

    private val repo = FavoriteRepository()

    private val _favorites = MutableStateFlow(repo.getFavorites())
    val favorites: StateFlow<List<FavoriteService>> = _favorites.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val categories: List<String> = listOf(ALL) + repo.getAllCategories()

    /** Daftar di grid setelah filter kategori dan pencarian. */
    val visibleFavorites: StateFlow<List<FavoriteService>> =
        combine(_favorites, _selectedCategory, _query) { list, category, query ->
            applyFilter(list, category, query)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = applyFilter(repo.getFavorites(), ALL, "")
        )

    private val _catalogQuery = MutableStateFlow("")
    val catalogQuery: StateFlow<String> = _catalogQuery.asStateFlow()

    private val _selectedCatalogCategory = MutableStateFlow(ALL)
    val selectedCatalogCategory: StateFlow<String> = _selectedCatalogCategory.asStateFlow()

    fun setCatalogQuery(q: String) {
        _catalogQuery.value = q
    }

    fun selectCatalogCategory(cat: String) {
        _selectedCatalogCategory.value = cat
    }

    /** Daftar seluruh jasa di katalog untuk halaman eksplorasi. */
    val visibleCatalogServices: StateFlow<List<FavoriteService>> =
        combine(_favorites, _selectedCatalogCategory, _catalogQuery) { _, category, query ->
            applyFilter(repo.getAllCatalog(), category, query)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repo.getAllCatalog()
        )

    private val _events = Channel<FavoriteEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastRemoved: Pair<FavoriteService, Int>? = null

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun getById(id: String): FavoriteService? = repo.getById(id)

    fun isFavorite(id: String): Boolean = repo.isFavorite(id)

    fun add(item: FavoriteService) {
        if (repo.add(item.id)) {
            _favorites.value = repo.getFavorites()
            _events.trySend(FavoriteEvent.Added(item))
        }
    }

    fun toggleFavorite(item: FavoriteService) {
        if (repo.isFavorite(item.id)) {
            remove(item)
        } else {
            add(item)
        }
    }

    fun remove(item: FavoriteService) {
        val index = repo.remove(item.id)
        lastRemoved = item to index
        _favorites.value = repo.getFavorites()
        _events.trySend(FavoriteEvent.Removed(item))
    }

    fun undoRemove() {
        lastRemoved?.let { (item, index) ->
            repo.restore(item.id, index)
            _favorites.value = repo.getFavorites()
        }
        lastRemoved = null
    }

    fun resetDemo() {
        repo.reset()
        _favorites.value = repo.getFavorites()
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
}
