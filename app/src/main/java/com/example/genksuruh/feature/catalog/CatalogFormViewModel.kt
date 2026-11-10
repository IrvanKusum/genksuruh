package com.example.genksuruh.feature.catalog

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.auth.AuthRepository
import com.example.genksuruh.core.model.Service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Membaca rentang jam dari teks, contoh: "08.00 - 16.00"
private val TIME_RANGE = Regex("""(\d{1,2})[.:](\d{2})\s*-\s*(\d{1,2})[.:](\d{2})""")

sealed interface PhotoState {
    object Empty : PhotoState
    data class Uploading(val progress: Int) : PhotoState
    data class Uploaded(val url: String?, val imageRes: Int?) : PhotoState
    data class Error(val message: String) : PhotoState
}

// State form selain teks yang diketik (teks ada di ViewModel sebagai mutableStateOf)
data class FormUiState(
    val isEdit: Boolean = false,
    val photo: PhotoState = PhotoState.Empty,
    val category: String = "",
    val areas: Set<String> = emptySet(),
    val startTime: String = "08:00",
    val endTime: String = "16:00",
    val photoError: String? = null,
    val titleError: String? = null,
    val categoryError: String? = null,
    val priceError: String? = null,
    val errorMessage: String? = null, // gagal menyimpan
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class CatalogFormViewModel @JvmOverloads constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CatalogRepository = CatalogRepository,
    private val auth: AuthRepository = AuthRepository
) : ViewModel() {

    private val editId: String? = savedStateHandle["serviceId"]

    // Bagian hari dari jam lama ("Senin - Jumat"), supaya tidak hilang saat edit.
    // Harus dideklarasikan SEBELUM blok init di bawah.
    private var dayLabel: String = "Setiap hari"

    // Jam lama yang tidak berbentuk rentang (mis. "Sesuai jadwal acara") dipertahankan
    // apa adanya selama jam tidak diubah.
    private var originalHours: String? = null
    private var timeEdited = false

    // Teks TextField -> mutableStateOf (dipakai validasi dan dikirim ke Repository)
    var title by mutableStateOf("")
        private set
    var priceText by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(FormUiState(isEdit = editId != null))
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    init {
        // Mode edit: isi form dari data yang ada
        editId?.let { id ->
            repository.getById(id)?.let { s ->
                title = s.title
                priceText = s.price.toString()
                description = s.description

                // Jam lama, contoh: "Senin - Jumat, 08.00 - 16.00"
                var start = "08:00"
                var end = "16:00"
                val match = TIME_RANGE.find(s.hours)
                if (match != null) {
                    val (h1, m1, h2, m2) = match.destructured
                    start = "${h1.padStart(2, '0')}:$m1"
                    end = "${h2.padStart(2, '0')}:$m2"
                    dayLabel = s.hours.substring(0, match.range.first)
                        .trim().trimEnd(',').trim()
                        .ifBlank { "Setiap hari" }
                } else {
                    originalHours = s.hours
                }

                _uiState.value = FormUiState(
                    isEdit = true,
                    photo = if (s.imageRes != null || s.imageUrl != null)
                        PhotoState.Uploaded(s.imageUrl, s.imageRes) else PhotoState.Empty,
                    category = s.category,
                    areas = s.areas.toSet(),
                    startTime = start,
                    endTime = end
                )
            }
        }
    }

    fun onTitleChange(v: String) {
        if (v.length <= 60) {
            title = v
            _uiState.update { it.copy(titleError = null) }
        }
    }

    fun onPriceChange(v: String) {
        if (v.length <= 7 && v.all { it.isDigit() }) {
            priceText = v
            _uiState.update { it.copy(priceError = null) }
        }
    }

    fun onDescriptionChange(v: String) {
        if (v.length <= 300) description = v
    }

    fun onCategoryChange(v: String) {
        _uiState.update { it.copy(category = v, categoryError = null) }
    }

    fun toggleArea(area: String) {
        _uiState.update {
            it.copy(areas = if (area in it.areas) it.areas - area else it.areas + area)
        }
    }

    fun onStartTimeChange(v: String) {
        timeEdited = true
        _uiState.update { it.copy(startTime = v) }
    }

    fun onEndTimeChange(v: String) {
        timeEdited = true
        _uiState.update { it.copy(endTime = v) }
    }

    // ---------- Foto ----------

    fun onPhotoPicked(uri: Uri, sizeBytes: Long) {
        if (sizeBytes > MAX_PHOTO_BYTES) {
            _uiState.update {
                it.copy(photo = PhotoState.Error("Gagal mengunggah. Ukuran file melebihi 2 MB."))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(photo = PhotoState.Uploading(0), photoError = null) }
            try {
                val url = repository.uploadPhoto(uri.toString()) { p ->
                    _uiState.update { it.copy(photo = PhotoState.Uploading(p)) }
                }
                _uiState.update { it.copy(photo = PhotoState.Uploaded(url, null)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(photo = PhotoState.Error(e.message ?: "Gagal mengunggah foto"))
                }
            }
        }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photo = PhotoState.Empty) }
    }

    // ---------- Simpan ----------

    fun save() {
        val s = _uiState.value
        val titleErr = if (title.isBlank()) "Judul wajib diisi" else null
        val categoryErr = if (s.category.isBlank()) "Kategori belum dipilih" else null
        val priceErr = if ((priceText.toIntOrNull() ?: 0) <= 0) "Harga wajib diisi" else null
        val photoErr = if (s.photo !is PhotoState.Uploaded) "Foto jasa wajib diunggah" else null

        if (titleErr != null || categoryErr != null || priceErr != null || photoErr != null) {
            _uiState.update {
                it.copy(
                    titleError = titleErr,
                    categoryError = categoryErr,
                    priceError = priceErr,
                    photoError = photoErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val old = editId?.let { repository.getById(it) }
                val photo = s.photo as PhotoState.Uploaded

                // Jam: pakai teks lama jika tidak bisa dibaca dan tidak diubah user,
                // selain itu susun dari hari lama + jam yang dipilih.
                val keepOldHours = originalHours
                val hours = if (keepOldHours != null && !timeEdited) {
                    keepOldHours
                } else {
                    "$dayLabel, ${s.startTime.replace(':', '.')} - ${s.endTime.replace(':', '.')}"
                }

                // Pemilik jasa baru = akun yang sedang login
                val owner = auth.account.value

                repository.save(
                    Service(
                        id = old?.id ?: "svc-${System.currentTimeMillis()}",
                        title = title.trim(),
                        category = s.category,
                        price = priceText.toInt(),
                        description = description.trim(),
                        areas = s.areas.toList(),
                        hours = hours,
                        providerId = old?.providerId ?: owner?.id ?: CatalogRepository.CURRENT_USER_ID,
                        providerName = old?.providerName ?: owner?.name ?: CatalogRepository.CURRENT_USER_NAME,
                        rating = old?.rating ?: 0.0,
                        reviewCount = old?.reviewCount ?: 0,
                        imageRes = photo.imageRes,
                        imageUrl = photo.url,
                        isFavorite = old?.isFavorite ?: false
                    )
                )
                _uiState.update { it.copy(saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // input user TIDAK dikosongkan: title, harga, dst. tetap ada
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal menyimpan jasa") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private companion object {
        const val MAX_PHOTO_BYTES = 2L * 1024 * 1024
    }
}