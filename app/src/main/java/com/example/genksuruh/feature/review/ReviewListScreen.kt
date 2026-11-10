package com.example.genksuruh.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.genksuruh.core.theme.*

@Composable
fun ReviewListScreen(
    vm: ReviewViewModel,
    onBack: () -> Unit,
    onWrite: () -> Unit,
    onEdit: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    // Dengarkan pesan (sukses kirim / hapus) dan tampilkan sebagai snackbar hijau
    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            val result = snackbarHost.showSnackbar(
                message = msg.text,
                actionLabel = msg.actionLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) vm.undoDelete()
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    Modifier.statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Ulasan", style = MaterialTheme.typography.titleMedium)
                        Text(vm.serviceName, style = MaterialTheme.typography.bodySmall, color = TextGray)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "Urutkan", tint = OceanBlue)
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                val mine = state.myReview
                Button(
                    onClick = if (mine != null) onEdit else onWrite,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral),
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (mine != null) "Edit Ulasan Kamu" else "Tulis Ulasan",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SuccessGreen,
                    contentColor = Color.White,
                    actionColor = Color.White,
                )
            }
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingSkeleton(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { RatingSummaryCard(state.average, state.total, state.distribution) }
                item {
                    StarFilterChips(state.distribution, state.total, state.filter, vm::selectFilter)
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${state.filterCount} ulasan",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f),
                        )
                        Text("Terbaru ▾", style = MaterialTheme.typography.labelMedium, color = OceanBlue)
                    }
                }

                val list = state.visibleReviews
                if (list.isEmpty()) {
                    item {
                        EmptyState(
                            filter = state.filter,
                            onWrite = onWrite,
                            onReset = { vm.selectFilter(null) },
                        )
                    }
                } else {
                    items(list, key = { it.id }) { review ->
                        ReviewCard(review, onClick = { selectedReview = review }, onEdit = onEdit, onDelete = vm::requestDelete)
                    }
                }
            }
        }
    }

    selectedReview?.let { review ->
        AlertDialog(
            onDismissRequest = { selectedReview = null },
            title = { Text("Ulasan ${review.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(review.date, color = TextGray, style = MaterialTheme.typography.bodySmall)
                    StarRow(review.rating, 18.dp)
                    Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                    if (review.isOwn) Text("Ini adalah ulasan kamu", color = OceanBlue, style = MaterialTheme.typography.labelMedium)
                }
            },
            confirmButton = { TextButton(onClick = { selectedReview = null }) { Text("Tutup") } },
            containerColor = Color.White,
        )
    }

    if (state.deleteState != DeleteState.Idle) {
        DeleteReviewDialog(
            serviceName = vm.serviceName,
            state = state.deleteState,
            onConfirm = vm::confirmDelete,
            onCancel = vm::cancelDelete,
        )
    }
}

@Composable
private fun LoadingSkeleton(modifier: Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonBox(Modifier.fillMaxWidth().height(130.dp))
        SkeletonBox(Modifier.fillMaxWidth(0.7f).height(36.dp))
        repeat(3) { SkeletonBox(Modifier.fillMaxWidth().height(110.dp)) }
    }
}

@Composable
private fun EmptyState(filter: Int?, onWrite: () -> Unit, onReset: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Star, null, tint = StarEmpty, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        if (filter == null) {
            Text("Belum ada ulasan", style = MaterialTheme.typography.titleMedium)
            Text(
                "Jadilah yang pertama mengulas jasa ini",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onWrite, colors = ButtonDefaults.buttonColors(containerColor = Coral)) {
                Text("Tulis Ulasan")
            }
        } else {
            Text("Belum ada ulasan $filter bintang", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onReset) { Text("Reset Filter", color = OceanBlue) }
        }
    }
}