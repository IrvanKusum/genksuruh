package com.example.genksuruh.feature.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.genksuruh.feature.review.Review
import com.example.genksuruh.feature.review.ReviewRepository

sealed interface OrdersUiState {
    object Loading : OrdersUiState
    object Empty : OrdersUiState
    data class Success(val orders: List<Order>) : OrdersUiState
}

/** Layar riwayat: satu StateFlow per tab agar pager menampilkan tiap tab sendiri. */
@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModel : ViewModel() {
    private val repo: TrackingRepository = FakeTrackingRepository
    private val role = MutableStateFlow(UserRole.USER)

    val tabStates: Map<OrderTab, StateFlow<OrdersUiState>> = OrderTab.values().associateWith { tab ->
        role.flatMapLatest { r ->
            repo.observeOrders(r, tab)
                .map<List<Order>, OrdersUiState> { list ->
                    if (list.isEmpty()) OrdersUiState.Empty else OrdersUiState.Success(list)
                }
                .onStart { emit(OrdersUiState.Loading) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState.Loading)
    }

    fun setRole(newRole: UserRole) {
        role.value = newRole
    }
}

/** Layar detail progres. */
class TrackingDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repo: TrackingRepository = FakeTrackingRepository
    private val orderId: String = checkNotNull(savedStateHandle.get<String>("orderId"))

    val detail: StateFlow<OrderDetail?> = repo.observeOrder(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Ter-update otomatis setelah ulasan dikirim/dihapus dari halaman Review.
    val ownReview: StateFlow<Review?> = ReviewRepository.observeOwnReview(orderId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun cancelOrder(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repo.cancelOrder(orderId)
            onDone()
        }
    }
}

/** Layar form update status (penyedia). */
class TrackingUpdateViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repo: TrackingRepository = FakeTrackingRepository
    private val orderId: String = checkNotNull(savedStateHandle.get<String>("orderId"))

    val detail: StateFlow<OrderDetail?> = repo.observeOrder(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var selectedStatus by mutableStateOf<OrderStatus?>(null)
        private set
    var note by mutableStateOf("")
        private set
    var isSaving by mutableStateOf(false)
        private set

    val canSave: Boolean get() = selectedStatus != null && !isSaving

    fun onStatusSelected(status: OrderStatus) {
        selectedStatus = status
    }

    fun onNoteChange(value: String) {
        if (value.length <= MAX_NOTE) note = value
    }

    fun save(onSaved: () -> Unit) {
        val status = selectedStatus ?: return
        viewModelScope.launch {
            isSaving = true
            repo.updateStatus(orderId, status, note)
            isSaving = false
            onSaved()
        }
    }

    companion object {
        const val MAX_NOTE = 200

        /** Status yang boleh dipilih penyedia (hanya maju). */
        val SELECTABLE = listOf(OrderStatus.DITERIMA, OrderStatus.DIKERJAKAN, OrderStatus.SELESAI)
    }
}