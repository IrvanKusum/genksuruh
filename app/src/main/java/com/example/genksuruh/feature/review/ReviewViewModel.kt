package com.example.genksuruh.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
) {
    val total: Int get() = (1..5).sumOf { distribution[it] ?: 0 }
    val average: Double
        get() = if (total == 0) 0.0 else (1..5).sumOf { star -> star * (distribution[star] ?: 0) }.toDouble() / total
    val visibleReviews: List<Review>
        get() = if (filter == null) reviews else reviews.filter { it.rating == filter }
    val filterCount: Int get() = if (filter == null) total else distribution[filter] ?: 0
    val myReview: Review? get() = reviews.firstOrNull { it.isOwn }
}

class ReviewViewModel(
    private val repository: ReviewRepository = ReviewRepository(),
) : ViewModel() {

    val serviceName = "Antar Makanan Kantin FILKOM"
    val providerName = "Dimas Pratama"
    val servicePrice = "Rp 8.000"

    /** Aktifkan hanya untuk menguji tampilan kegagalan tanpa backend. */
    var simulateFailure: Boolean = false

    private val _state = MutableStateFlow(
        ReviewUiState(
            reviews = repository.getInitialReviews(),
            distribution = repository.getInitialDistribution(),
        )
    )
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private val _messages = Channel<UiMessage>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private var nextId = (_state.value.reviews.maxOfOrNull { it.id } ?: 0) + 1
    private var lastDeleted: Review? = null

    init {
        viewModelScope.launch {
            delay(650)
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectFilter(star: Int?) {
        _state.update { it.copy(filter = star?.takeIf { value -> value in 1..5 }) }
    }

    fun findReview(id: Int): Review? = _state.value.reviews.firstOrNull { it.id == id }

    /** Create dan Update. Satu akun hanya boleh memiliki satu ulasan untuk jasa ini. */
    fun submitReview(rating: Int, comment: String, onResult: (Boolean) -> Unit) {
        val cleanComment = comment.trim()
        if (rating !in 1..5 || cleanComment.length !in 10..300) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            delay(450) // simulasi proses request
            if (simulateFailure) {
                onResult(false)
                return@launch
            }

            val current = _state.value.myReview
            _state.update { old ->
                val counts = old.distribution.toMutableMap()
                val updatedReviews = if (current == null) {
                    counts[rating] = (counts[rating] ?: 0) + 1
                    val newReview = Review(
                        id = nextId++, name = "Irvan", rating = rating,
                        comment = cleanComment, date = "Hari ini", isOwn = true,
                    )
                    listOf(newReview) + old.reviews
                } else {
                    if (current.rating != rating) {
                        counts[current.rating] = ((counts[current.rating] ?: 0) - 1).coerceAtLeast(0)
                        counts[rating] = (counts[rating] ?: 0) + 1
                    }
                    old.reviews.map { review ->
                        if (review.id == current.id) review.copy(rating = rating, comment = cleanComment, date = "Hari ini") else review
                    }
                }
                old.copy(reviews = updatedReviews, distribution = counts)
            }
            _messages.send(UiMessage(if (current == null) "Ulasan berhasil dikirim" else "Perubahan ulasan berhasil disimpan"))
            onResult(true)
        }
    }

    fun requestDelete() {
        if (_state.value.myReview != null) _state.update { it.copy(deleteState = DeleteState.Confirm) }
    }

    fun cancelDelete() = _state.update { it.copy(deleteState = DeleteState.Idle) }

    fun confirmDelete() {
        val target = _state.value.myReview ?: return
        if (_state.value.deleteState == DeleteState.Loading) return
        viewModelScope.launch {
            _state.update { it.copy(deleteState = DeleteState.Loading) }
            delay(400)
            if (simulateFailure) {
                _state.update { it.copy(deleteState = DeleteState.Error) }
                return@launch
            }
            lastDeleted = target
            _state.update { old ->
                val counts = old.distribution.toMutableMap()
                counts[target.rating] = ((counts[target.rating] ?: 0) - 1).coerceAtLeast(0)
                old.copy(
                    reviews = old.reviews.filterNot { it.id == target.id },
                    distribution = counts,
                    deleteState = DeleteState.Idle,
                )
            }
            _messages.send(UiMessage("Ulasan berhasil dihapus", actionLabel = "Urungkan"))
        }
    }

    fun undoDelete() {
        val deleted = lastDeleted ?: return
        if (_state.value.myReview != null) return
        _state.update { old ->
            val counts = old.distribution.toMutableMap()
            counts[deleted.rating] = (counts[deleted.rating] ?: 0) + 1
            old.copy(reviews = listOf(deleted) + old.reviews, distribution = counts)
        }
        lastDeleted = null
    }
}
