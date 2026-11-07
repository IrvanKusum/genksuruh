package com.example.genksuruh.feature.catalog

import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

// MODEL (Repository): satu-satunya pintu data jasa.
// Progress 1: data dummy. Progress 2: ganti isi fungsi dengan pemanggilan Supabase.
object CatalogRepository {

    // Pengguna dummy yang sedang login
    const val CURRENT_USER_ID = "usr-irvan"
    const val CURRENT_USER_NAME = "Irvan Nabil"

    // "Server" dummy. Nanti diganti tabel `services` di Supabase.
    private var serverData: List<Service> = DummyServices.list

    // Data yang sudah diterima aplikasi. Kosong sampai loadServices() dipanggil.
    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    private fun commit(newList: List<Service>) {
        serverData = newList
        _services.value = newList
    }

    // Simulasi GET
    suspend fun loadServices(simulateFailure: Boolean = false) {
        delay(800) // anggap ini waktu tunggu jaringan
        if (simulateFailure) throw IOException("Gagal terhubung ke server (simulasi)")
        _services.value = serverData
    }

    fun getById(id: String): Service? = serverData.find { it.id == id }

    suspend fun toggleFavorite(id: String) {
        commit(serverData.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it })
    }

    // Simulasi upload foto. Progress 2: upload ke Supabase Storage, kembalikan URL publik.
    suspend fun uploadPhoto(localUri: String, onProgress: (Int) -> Unit): String {
        for (p in 0..100 step 10) {
            onProgress(p)
            delay(120)
        }
        return localUri
    }

    // Simulasi POST/PUT. Judul yang mengandung "gagal" ditolak, untuk menguji state error.
    suspend fun save(service: Service) {
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

    // Simulasi DELETE (dipakai di langkah Dialog Hapus nanti)
    suspend fun delete(id: String, simulateFailure: Boolean = false) {
        delay(600)
        if (simulateFailure) throw IOException("Gagal menghapus jasa (simulasi)")
        commit(serverData.filterNot { it.id == id })
    }
}