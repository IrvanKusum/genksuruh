package com.example.genksuruh.feature.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.util.toRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesScreen(
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onServiceClick: (String) -> Unit,
    viewModel: AdminServicesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelola Jasa", style = MaterialTheme.typography.titleLarge)
                        if (!state.isLoading && state.errorMessage == null) {
                            Text(
                                "${state.services.size} jasa di katalog",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = Secondary500,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Tambah Jasa", fontWeight = FontWeight.SemiBold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.errorMessage != null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Gagal memuat jasa", style = MaterialTheme.typography.titleMedium)
                Text(
                    state.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::load) { Text("Coba Lagi") }
            }

            state.services.isEmpty() -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Belum ada jasa", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tekan Tambah Jasa untuk membuat jasa pertama.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.services, key = { it.id }) { service ->
                    AdminServiceCard(
                        service = service,
                        onClick = { onServiceClick(service.id) },
                        onEdit = { onEditClick(service.id) },
                        onDelete = { viewModel.onDeleteClick(service) }
                    )
                }
            }
        }
    }

    state.deleteTarget?.let { target ->
        DeleteDialog(
            title = target.title,
            deleteState = state.deleteState,
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteDismiss
        )
    }
}

@Composable
private fun AdminServiceCard(
    service: Service,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Thumbnail(service)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        service.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            service.price.toRupiah(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.Star,
                            null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            " ${service.rating}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hapus")
                }
            }
        }
    }
}

@Composable
private fun Thumbnail(service: Service) {
    val modifier = Modifier
        .size(72.dp)
        .clip(RoundedCornerShape(12.dp))
    when {
        service.imageRes != null -> Image(
            painter = painterResource(service.imageRes),
            contentDescription = service.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        service.imageUrl != null -> AsyncImage(
            model = service.imageUrl,
            contentDescription = service.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Image, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DeleteDialog(
    title: String,
    deleteState: DeleteState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val loading = deleteState is DeleteState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus jasa?") },
        text = {
            Column {
                Text("\"$title\" akan dihapus dari katalog dan tidak bisa dikembalikan.")
                when (deleteState) {
                    is DeleteState.Loading -> {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Menghapus...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    is DeleteState.Error -> {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            deleteState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    DeleteState.Idle -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !loading) {
                Text(
                    if (deleteState is DeleteState.Error) "Coba Lagi" else "Hapus",
                    color = if (loading) Color.Unspecified else MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) { Text("Batal") }
        }
    )
}