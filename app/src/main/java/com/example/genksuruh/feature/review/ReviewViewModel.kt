
package com.example.genksuruh.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val reviews: List<Review> = emptyList(),
    val distribution: Map<Int, Int> = emptyMap(),
    val filter: Int? = null,
    val isLoading: Boolean = true,
    val deleteState: DeleteState = DeleteState.Idle,
    val order: ReviewOrderDto? = null,
    val errorMessage: String? = null
) {
    val total: Int
        get() = reviews.size

    val average: Double
        get() = if (reviews.isEmpty()) {
            0.0
        } else {
            reviews.sumOf { it.rating }.toDouble() / reviews.size
        }

    val visibleReviews: List<Review>
        get() = if (filter == null) {
            reviews
        } else {
            reviews.filter { it.rating == filter }
        }

    val filterCount: Int
        get() = if (filter == null) {
            total
        } else {
            distribution[filter] ?: 0
        }

    val myReview: Review?
        get() = reviews.firstOrNull {
            it.isOwn && it.orderId == order?.id
        }
}

class ReviewViewModel(
    val orderId: String,
    private val repository: ReviewRepository = SupabaseReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private val _messages = Channel<UiMessage>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private var lastDeleted: Review? = null

    // Informasi jasa sementara berdasarkan data yang tersedia.
    // Nama provider dan harga belum tersedia pada DTO orders.
    val serviceName: String
        get() = state.value.order?.let {
            "Jasa ${it.serviceId.take(8)}"
        } ?: "Informasi jasa"

    val providerName: String = "-"
    val servicePrice: String = "-"
    val serviceEmoji: String = "⭐"

    init {
        refresh()
    }

    /**
     * Mengambil pesanan dan ulasan dari Supabase.
     */
    fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val order = repository.getOrderForReview(orderId)
                    ?: throw IllegalStateException(
                        "Pesanan tidak ditemukan, bukan milik kamu, atau belum berstatus SELESAI."
                    )

                val reviews = repository.getReviews(order.serviceId)

                _state.update {
                    it.copy(
                        order = order,
                        reviews = reviews,
                        distribution = calculateDistribution(reviews),
                        isLoading = false,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: "Gagal mengambil data ulasan."
                    )
                }
            }
        }
    }

    /**
     * Menghitung jumlah ulasan setiap bintang.
     */
    private fun calculateDistribution(
        reviews: List<Review>
    ): Map<Int, Int> {
        return (1..5).associateWith { star ->
            reviews.count { it.rating == star }
        }
    }

    fun selectFilter(star: Int?) {
        _state.update {
            it.copy(
                filter = star?.takeIf { value -> value in 1..5 }
            )
        }
    }

    fun findReview(id: String): Review? {
        return _state.value.reviews.firstOrNull {
            it.id == id
        }
    }

    /**
     * INSERT atau UPDATE ulasan.
     */
    fun submitReview(
        rating: Int,
        comment: String,
        onResult: (Boolean) -> Unit
    ) {
        val cleanComment = comment.trim()

        if (
            rating !in 1..5 ||
            cleanComment.length !in 10..300
        ) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val savedReview = repository.submitReview(
                    orderId = orderId,
                    rating = rating,
                    comment = cleanComment
                )

                val updatedReviews = _state.value.reviews
                    .filterNot { it.orderId == orderId } +
                        savedReview

                val sortedReviews = updatedReviews
                    .sortedByDescending { it.createdAtIso }

                _state.update {
                    it.copy(
                        reviews = sortedReviews,
                        distribution = calculateDistribution(sortedReviews),
                        errorMessage = null
                    )
                }

                _messages.send(
                    UiMessage(
                        if (savedReview.orderId == orderId) {
                            "Ulasan berhasil disimpan"
                        } else {
                            "Ulasan berhasil dikirim"
                        }
                    )
                )

                onResult(true)

            } catch (e: Exception) {
                _messages.send(
                    UiMessage(
                        e.message ?: "Gagal menyimpan ulasan."
                    )
                )

                onResult(false)
            }
        }
    }

    /**
     * Memunculkan konfirmasi hapus.
     */
    fun requestDelete() {
        if (_state.value.myReview != null) {
            _state.update {
                it.copy(deleteState = DeleteState.Confirm)
            }
        }
    }

    fun cancelDelete() {
        _state.update {
            it.copy(deleteState = DeleteState.Idle)
        }
    }

    /**
     * Menghapus ulasan dari Supabase.
     */
    fun confirmDelete() {
        val target = _state.value.myReview ?: return

        if (_state.value.deleteState == DeleteState.Loading) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(deleteState = DeleteState.Loading)
            }

            try {
                repository.deleteReview(target)

                lastDeleted = target

                val updatedReviews = _state.value.reviews
                    .filterNot { it.id == target.id }

                _state.update {
                    it.copy(
                        reviews = updatedReviews,
                        distribution = calculateDistribution(updatedReviews),
                        deleteState = DeleteState.Idle
                    )
                }

                _messages.send(
                    UiMessage(
                        text = "Ulasan berhasil dihapus",
                        actionLabel = "Urungkan"
                    )
                )

            } catch (e: Exception) {
                _state.update {
                    it.copy(deleteState = DeleteState.Error)
                }

                _messages.send(
                    UiMessage(
                        e.message ?: "Gagal menghapus ulasan."
                    )
                )
            }
        }
    }

    /**
     * Mengembalikan ulasan yang telah dihapus.
     */
    fun undoDelete() {
        val deleted = lastDeleted ?: return

        viewModelScope.launch {
            try {
                repository.restoreReview(deleted)

                val updatedReviews = _state.value.reviews + deleted

                _state.update {
                    it.copy(
                        reviews = updatedReviews.sortedByDescending {
                                review -> review.createdAtIso
                        },
                        distribution = calculateDistribution(updatedReviews)
                    )
                }

                lastDeleted = null

                _messages.send(
                    UiMessage("Ulasan berhasil dikembalikan")
                )

            } catch (e: Exception) {
                _messages.send(
                    UiMessage("Gagal mengembalikan ulasan.")
                )
            }
        }
    }
}

/**
 * Factory ViewModel.
 */
class ReviewViewModelFactory(
    private val orderId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(orderId) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
