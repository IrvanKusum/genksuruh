package com.example.genksuruh.feature.catalog

import android.util.Log
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.network.StorageUploader
import com.example.genksuruh.core.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import java.util.UUID

@Serializable
private data class ProfileName(
    val id: String,
    val name: String = "Penyedia Jasa"
)

@Serializable
private data class ServiceRow(
    val id: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String,
    val areas: List<String> = emptyList(),
    val hours: String = "",
    @SerialName("provider_id") val providerId: String = "",
    @SerialName("provider_name") val providerName: String? = null,
    @SerialName("provider_phone") val providerPhone: String = "085895089845",
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null
) {
    fun toService(fallbackProviderName: String): Service = Service(
        id = id,
        title = title,
        category = category,
        price = price,
        description = description,
        areas = areas,
        hours = hours,
        providerId = providerId,
        providerName = providerName.takeUnless { it.isNullOrBlank() } ?: fallbackProviderName,
        providerPhone = providerPhone,
        rating = rating,
        reviewCount = reviewCount,
        imageUrl = imageUrl
    )
}

@Serializable
private data class ServiceInsert(
    @SerialName("provider_id") val providerId: String,
    @SerialName("provider_name") val providerName: String,
    @SerialName("provider_phone") val providerPhone: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String,
    val areas: List<String>,
    val hours: String,
    @SerialName("image_url") val imageUrl: String?
)

@Serializable
private data class ServiceUpdate(
    @SerialName("provider_name") val providerName: String,
    @SerialName("provider_phone") val providerPhone: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String,
    val areas: List<String>,
    val hours: String,
    @SerialName("image_url") val imageUrl: String?
)

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
            val list = rows.map { row: ServiceRow ->
                val providerName = profileMap[row.providerId] ?: "Penyedia Jasa"
                row.toService(providerName)
            }
            _services.value = list
        } catch (e: Exception) {
            Log.e("SupabaseCatalogRepo", "loadServices gagal: ${e.message}", e)
            throw e
        }
    }

    override fun getById(id: String): Service? = _services.value.find { it.id == id }

    override suspend fun toggleFavorite(id: String) {
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
