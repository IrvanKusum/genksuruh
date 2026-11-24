package com.example.genksuruh.feature.catalog

import android.util.Log
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.network.StorageUploader
import com.example.genksuruh.core.network.SupabaseClient
import com.example.genksuruh.feature.favorite.data.FavoriteRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.UUID

// Kontrak Repository Katalog Jasa. ViewModel bergantung pada interface ini.
interface CatalogRepository {
    val services: StateFlow<List<Service>>
    suspend fun loadServices(simulateFailure: Boolean = false)
    fun getById(id: String): Service?
    suspend fun toggleFavorite(id: String)
    suspend fun uploadPhoto(bytes: ByteArray, mimeType: String, onProgress: (Int) -> Unit): String
    suspend fun save(service: Service)
    suspend fun delete(id: String, simulateFailure: Boolean = false)

    companion object : CatalogRepository by SupabaseCatalogRepository {
        const val CURRENT_USER_ID = "usr-irvan"
        const val CURRENT_USER_NAME = "Irvan Nabil"
    }
}

// Implementasi Supabase Repository untuk Katalog Jasa
object SupabaseCatalogRepository : CatalogRepository {

    private val db get() = SupabaseClient.client.postgrest

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    override val services: StateFlow<List<Service>> = _services.asStateFlow()

    override suspend fun loadServices(simulateFailure: Boolean) {
        if (simulateFailure) throw IOException("Gagal terhubung ke server (simulasi)")
        try {
            val profileMap = runCatching {
                db.from("profiles").select().decodeList<ProfileName>()
                    .associateBy({ p: ProfileName -> p.id }, { p: ProfileName -> p.name })
            }.getOrDefault(emptyMap())

            val rows = db.from("services").select().decodeList<ServiceRow>()
            runCatching { FavoriteRepository.reload() }
            val list = rows.map { row: ServiceRow ->
                val providerName = profileMap[row.providerId] ?: "Penyedia Jasa"
                row.toService(providerName).copy(isFavorite = FavoriteRepository.isFavorite(row.id))
            }
            _services.value = list
        } catch (e: Exception) {
            Log.e("SupabaseCatalogRepo", "loadServices gagal: ${e.message}", e)
            throw e
        }
    }

    override fun getById(id: String): Service? = _services.value.find { it.id == id }

    override suspend fun toggleFavorite(id: String) {
        // Simpan ke tabel favorites (Supabase) lewat FavoriteRepository
        if (FavoriteRepository.isFavorite(id)) FavoriteRepository.remove(id) else FavoriteRepository.add(id)
        _services.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    override suspend fun uploadPhoto(bytes: ByteArray, mimeType: String, onProgress: (Int) -> Unit): String {
        onProgress(30)
        val ext = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val path = "services/${UUID.randomUUID()}.$ext"
        onProgress(60)
        val url = StorageUploader.upload(path, bytes, overwrite = true)
        onProgress(100)
        return url
    }

    override suspend fun save(service: Service) {
        if (service.title.contains("gagal", ignoreCase = true)) {
            throw IOException("Server menolak jasa \"${service.title}\" (simulasi)")
        }
        val exists = _services.value.any { it.id == service.id }
        if (exists) {
            val updateDto = ServiceUpdate(
                providerName = service.providerName,
                providerPhone = service.providerPhone,
                title = service.title,
                category = service.category,
                price = service.price,
                description = service.description,
                areas = service.areas,
                hours = service.hours,
                imageUrl = service.imageUrl
            )
            db.from("services").update(updateDto) {
                filter {
                    eq("id", service.id)
                }
            }
        } else {
            val insertDto = ServiceInsert(
                providerId = service.providerId,
                providerName = service.providerName,
                providerPhone = service.providerPhone,
                title = service.title,
                category = service.category,
                price = service.price,
                description = service.description,
                areas = service.areas,
                hours = service.hours,
                imageUrl = service.imageUrl
            )
            db.from("services").insert(insertDto)
        }
        loadServices()
    }

    override suspend fun delete(id: String, simulateFailure: Boolean) {
        if (simulateFailure) throw IOException("Gagal menghapus jasa (simulasi)")
        db.from("services").delete {
            filter {
                eq("id", id)
            }
        }
        loadServices()
    }
}