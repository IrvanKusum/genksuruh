package com.example.genksuruh.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val StarColor = Color(0xFFF5A623)

data class ServiceReviewsState(
    val isLoading: Boolean = true,
    val reviews: List<Review> = emptyList(),
    val error: String? = null
) {
    val total: Int get() = reviews.size
    val average: Double
        get() = if (reviews.isEmpty()) 0.0 else reviews.sumOf { it.rating }.toDouble() / reviews.size
}

/** Daftar ulasan publik untuk satu jasa (dilihat siapa pun yang membuka detail jasa). */
class ServiceReviewsViewModel @JvmOverloads constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReviewRepository = SupabaseReviewRepository
) : ViewModel() {

    private val serviceId: String = checkNotNull(savedStateHandle["serviceId"])

    private val _state = MutableStateFlow(ServiceReviewsState())
    val state: StateFlow<ServiceReviewsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val list = repository.getReviews(serviceId)
                _state.update { it.copy(isLoading = false, reviews = list) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Gagal memuat ulasan")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReviewsScreen(
    onBack: () -> Unit,
    viewModel: ServiceReviewsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ulasan Jasa", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load) { Text("Coba lagi") }
                }

                state.reviews.isEmpty() -> Text(
                    "Belum ada ulasan untuk jasa ini.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = StarColor, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                String.format("%.1f", state.average),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "dari ${state.total} ulasan",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    items(state.reviews, key = { it.id }) { review ->
                        ReviewItem(review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (review.isOwn) "${review.name} (Kamu)" else review.name,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(review.date, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Row {
                repeat(5) { i ->
                    Icon(
                        Icons.Rounded.Star, null,
                        tint = if (i < review.rating) StarColor else Color(0xFFD9DEE5),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}