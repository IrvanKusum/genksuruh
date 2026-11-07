package com.example.genksuruh.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.feature.catalog.CatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State dialog hapus: default / loading / gagal (sukses = dialog tertutup + snackbar)
sealed interface DeleteState {
    object Idle : DeleteState
    object Loading : DeleteState
    data class Error(val message: String) : DeleteState
}

data class AdminUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val services: List<Service> = emptyList(),
    val deleteTarget: Service? = null,
    val deleteState: DeleteState = DeleteState.Idle,
    val message: String? = null
)

private data class AdminLocal(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val deleteTarget: Service? = null,
    val deleteState: DeleteState = DeleteState.Idle,
    val message: String? = null
)

class AdminServicesViewModel(
    private val repository: CatalogRepository = CatalogRepository
) : ViewModel() {

    private val local = MutableStateFlow(AdminLocal())

    val uiState: StateFlow<AdminUiState> = combine(repository.services, local) { list, l ->
        AdminUiState(
            isLoading = l.isLoading,
            errorMessage = l.errorMessage,
            services = list,
            deleteTarget = l.deleteTarget,
            deleteState = l.deleteState,
            message = l.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUiState())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            local.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.loadServices()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                local.update { it.copy(errorMessage = e.message ?: "Gagal memuat jasa") }
            } finally {
                local.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDeleteClick(service: Service) {
        local.update { it.copy(deleteTarget = service, deleteState = DeleteState.Idle) }
    }

    fun onDeleteDismiss() {
        if (local.value.deleteState is DeleteState.Loading) return
        local.update { it.copy(deleteTarget = null, deleteState = DeleteState.Idle) }
    }

    fun onDeleteConfirm() {
        val target = local.value.deleteTarget ?: return
        if (local.value.deleteState is DeleteState.Loading) return

        viewModelScope.launch {
            local.update { it.copy(deleteState = DeleteState.Loading) }
            try {
                // Judul mengandung "gagal" -> simulasi error, untuk menguji state gagal
                repository.delete(
                    id = target.id,
                    simulateFailure = target.title.contains("gagal", ignoreCase = true)
                )
                local.update {
                    it.copy(
                        deleteTarget = null,
                        deleteState = DeleteState.Idle,
                        message = "\"${target.title}\" berhasil dihapus"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                local.update {
                    it.copy(deleteState = DeleteState.Error(e.message ?: "Gagal menghapus jasa"))
                }
            }
        }
    }

    fun onMessageShown() {
        local.update { it.copy(message = null) }
    }
}