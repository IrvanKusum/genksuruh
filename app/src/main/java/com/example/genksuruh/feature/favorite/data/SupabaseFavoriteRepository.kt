package com.example.genksuruh.feature.favorite.data

import android.util.Log
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.network.SupabaseClient
import com.example.genksuruh.feature.catalog.DummyServices
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val TAG = "SupabaseFavoriteRepo"

@Serializable
data class FavoriteRow(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("created_at") val createdAt: String? = null
)

class SupabaseFavoriteRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : FavoriteRepository {

    companion object {
        const val DEFAULT_USER_ID = "usr-mahasiswa"
    }

    private val db get() = SupabaseClient.client.postgrest

    val currentUserId: String
        get() = try {
            SupabaseClient.client.auth.currentUserOrNull()?.id ?: DEFAULT_USER_ID
        } catch (_: Exception) {
            DEFAULT_USER_ID
        }

    private var allServices: List<Service> = DummyServices.list
    private val favoriteIds = mutableListOf<String>()

    private val _favoritesFlow = MutableStateFlow<List<Service>>(emptyList())
    val favoritesFlow: StateFlow<List<Service>> = _favoritesFlow.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            try {
                // 1. Ambil katalog services dari Supabase
                val servicesFromDb = db.from("services")
                    .select()
                    .decodeList<Service>()
                if (servicesFromDb.isNotEmpty()) {
                    allServices = servicesFromDb
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal memuat services dari Supabase, pakai dummy fallback: ${e.message}")
            }

            try {
                // 2. Ambil baris favorites milik user dari Supabase
                val rows = db.from("favorites")
                    .select {
                        filter { eq("user_id", currentUserId) }
                    }
                    .decodeList<FavoriteRow>()

                favoriteIds.clear()
                favoriteIds.addAll(rows.map { it.serviceId })
                refreshFavoritesFlow()
            } catch (e: Exception) {
                Log.w(TAG, "Gagal memuat favorites dari Supabase, fallback memori: ${e.message}")
            }
        }
    }

    private fun refreshFavoritesFlow() {
        val favList = favoriteIds.mapNotNull { id ->
            allServices.find { it.id == id }?.copy(isFavorite = true)
        }
        _favoritesFlow.value = favList
    }

    override fun getFavorites(): List<Service> {
        return favoriteIds.mapNotNull { id ->
            allServices.find { it.id == id }?.copy(isFavorite = true)
        }
    }

    override fun getAllCatalog(): List<Service> {
        return allServices.map { s ->
            s.copy(isFavorite = s.id in favoriteIds)
        }
    }

    override fun getAllCategories(): List<String> = allServices.map { it.category }.distinct()

    override fun getById(id: String): Service? {
        val s = allServices.find { it.id == id } ?: return null
        return s.copy(isFavorite = s.id in favoriteIds)
    }

    override fun isFavorite(id: String): Boolean = id in favoriteIds

    override fun add(id: String): Boolean {
        if (id !in favoriteIds && allServices.any { it.id == id }) {
            favoriteIds.add(id)
            refreshFavoritesFlow()

            // Asynchronous insert ke Supabase
            scope.launch {
                try {
                    db.from("favorites").insert(buildJsonObject {
                        put("user_id", currentUserId)
                        put("service_id", id)
                    })
                    Log.d(TAG, "Berhasil simpan favorit $id ke Supabase")
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal simpan favorit ke Supabase: ${e.message}", e)
                }
            }
            return true
        }
        return false
    }

    override fun remove(id: String): Int {
        val index = favoriteIds.indexOf(id)
        if (index >= 0) {
            favoriteIds.removeAt(index)
            refreshFavoritesFlow()

            // Asynchronous delete dari Supabase
            scope.launch {
                try {
                    db.from("favorites").delete {
                        filter {
                            eq("user_id", currentUserId)
                            eq("service_id", id)
                        }
                    }
                    Log.d(TAG, "Berhasil hapus favorit $id dari Supabase")
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal hapus favorit dari Supabase: ${e.message}", e)
                }
            }
        }
        return index
    }

    override fun restore(id: String, index: Int) {
        if (id in favoriteIds) return
        favoriteIds.add(index.coerceIn(0, favoriteIds.size), id)
        refreshFavoritesFlow()

        scope.launch {
            try {
                db.from("favorites").insert(buildJsonObject {
                    put("user_id", currentUserId)
                    put("service_id", id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "Gagal restore favorit ke Supabase: ${e.message}", e)
            }
        }
    }

    override fun reset() {
        favoriteIds.clear()
        refreshFavoritesFlow()
    }
}

