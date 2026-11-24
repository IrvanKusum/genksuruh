package com.example.genksuruh.feature.favorite.data

import com.example.genksuruh.core.model.Service
import com.example.genksuruh.feature.catalog.DummyServices

// Kontrak Repository Favorit
interface FavoriteRepository {
    fun getFavorites(): List<Service>
    fun getAllCatalog(): List<Service>
    fun getAllCategories(): List<String>
    fun getById(id: String): Service?
    fun isFavorite(id: String): Boolean
    fun add(id: String): Boolean
    fun remove(id: String): Int
    fun restore(id: String, index: Int)
    fun reset()

    companion object : FavoriteRepository by SupabaseFavoriteRepository() {
        val defaultInstance: FavoriteRepository = this
    }
}

/**
 * Sumber data DUMMY (di memori) untuk keperluan fallback dan testing.
 */
object DummyFavoriteRepository : FavoriteRepository {

    private val allServices = DummyServices.list

    private val favoriteIds = mutableListOf("svc-1", "svc-2", "svc-3", "svc-5")

    override fun getFavorites(): List<Service> =
        favoriteIds.mapNotNull { id -> allServices.find { it.id == id } }

    override fun getAllCatalog(): List<Service> = allServices

    override fun getAllCategories(): List<String> = allServices.map { it.category }.distinct()

    override fun getById(id: String): Service? = allServices.find { it.id == id }

    override fun isFavorite(id: String): Boolean = id in favoriteIds

    override fun add(id: String): Boolean {
        if (id !in favoriteIds && allServices.any { it.id == id }) {
            favoriteIds.add(id)
            return true
        }
        return false
    }

    override fun remove(id: String): Int {
        val index = favoriteIds.indexOf(id)
        if (index >= 0) favoriteIds.removeAt(index)
        return index
    }

    override fun restore(id: String, index: Int) {
        if (id in favoriteIds) return
        favoriteIds.add(index.coerceIn(0, favoriteIds.size), id)
    }

    override fun reset() {
        favoriteIds.clear()
        favoriteIds.addAll(listOf("svc-1", "svc-2", "svc-3", "svc-5"))
    }
}
