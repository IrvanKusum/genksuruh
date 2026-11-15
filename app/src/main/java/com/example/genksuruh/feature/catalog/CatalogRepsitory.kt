package com.example.genksuruh.feature.catalog

import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

// Kontrak Repository Katalog Jasa. ViewModel bergantung pada interface ini.
interface CatalogRepository {
    val services: StateFlow<List<Service>>
    suspend fun loadServices(simulateFailure: Boolean = false)
    fun getById(id: String): Service?
    suspend fun toggleFavorite(id: String)
    suspend fun uploadPhoto(localUri: String, onProgress: (Int) -> Unit): String
    suspend fun save(service: Service)
    suspend fun delete(id: String, simulateFailure: Boolean = false)

    companion object : CatalogRepository by DummyCatalogRepository {
        const val CURRENT_USER_ID = "usr-irvan"
        const val CURRENT_USER_NAME = "Irvan Nabil"
    }
}

// Implementasi Dummy / In-Memory
object DummyCatalogRepository : CatalogRepository {

    private var serverData: List<Service> = DummyServices.list

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    override val services: StateFlow<List<Service>> = _services.asStateFlow()

    private fun commit(newList: List<Service>) {
        serverData = newList
        _services.value = newList
    }

    override suspend fun loadServices(simulateFailure: Boolean) {
        delay(800)
        if (simulateFailure) throw IOException("Gagal terhubung ke server (simulasi)")
        _services.value = serverData
    }

    override fun getById(id: String): Service? = serverData.find { it.id == id }

    override suspend fun toggleFavorite(id: String) {
        commit(serverData.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it })
    }

    override suspend fun uploadPhoto(localUri: String, onProgress: (Int) -> Unit): String {
        for (p in 0..100 step 10) {
            onProgress(p)
            delay(120)
        }
        return localUri
    }

    override suspend fun save(service: Service) {
        delay(700)
        if (service.title.contains("gagal", ignoreCase = true)) {
            throw IOException("Server menolak jasa \"${service.title}\" (simulasi)")
        }
        val exists = serverData.any { it.id == service.id }
        commit(
            if (exists) serverData.map { if (it.id == service.id) service else it }
            else listOf(service) + serverData
        )
    }

    override suspend fun delete(id: String, simulateFailure: Boolean) {
        delay(600)
        if (simulateFailure) throw IOException("Gagal menghapus jasa (simulasi)")
        commit(serverData.filterNot { it.id == id })
    }
}
