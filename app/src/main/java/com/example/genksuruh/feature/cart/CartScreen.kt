package com.example.genksuruh.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.feature.cart.components.CartItemCard

// ============================================================
// MAIN SCREEN
// ============================================================

/**
 * Screen utama Keranjang (Cart).
 * Hanya bertugas mengumpulkan state & meneruskan aksi -- semua tampilan ada di [CartScreenContent].
 */
@Composable
fun CartScreen(
    onNavigateToItemDetail: (itemId: String) -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    onNavigateToCatalog: () -> Unit = {},
    onNavigateToPromo: () -> Unit = {},
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CartScreenContent(
        uiState = uiState,
        onToggleSelection = viewModel::toggleItemSelection,
        onSelectAll = viewModel::selectAllItems,
        onIncreaseQuantity = viewModel::increaseQuantity,
        onDecreaseQuantity = viewModel::decreaseQuantity,
        onDeleteItemClick = viewModel::removeItem,
        onConfirmDelete = viewModel::confirmRemoveItem,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onUndoRemove = viewModel::undoRemoveItem,
        onDismissUndoSnackbar = viewModel::dismissUndoSnackbar,
        onRetryClick = viewModel::loadCart,
        onEditItemClick = onNavigateToItemDetail,
        onCheckoutClick = onNavigateToCheckout,
        onExploreServicesClick = onNavigateToCatalog,
        onViewVouchersClick = onNavigateToPromo,
        onDismissNotification = viewModel::dismissNotificationMessage
    )
}

/**
 * Composable stateless yang menampilkan seluruh 8 kondisi Keranjang berdasarkan [uiState].
 * Dipisah dari [CartScreen] supaya mudah di-preview dan diuji tanpa ViewModel/Supabase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreenContent(
    uiState: CartUiState,
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onDeleteItemClick: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onUndoRemove: () -> Unit,
    onDismissUndoSnackbar: () -> Unit,
    onRetryClick: () -> Unit,
    onEditItemClick: (String) -> Unit,
    onCheckoutClick: () -> Unit,
    onExploreServicesClick: () -> Unit,
    onViewVouchersClick: () -> Unit = {},
    onDismissNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Memunculkan Snackbar untuk userNotificationMessage (e.g. "Pesanan berhasil disimpan di keranjang" / "Detail pesanan berhasil diedit")
    LaunchedEffect(uiState.userNotificationMessage) {
        val msg = uiState.userNotificationMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            onDismissNotification()
        }
    }

    // Memunculkan Snackbar Undo setiap kali ViewModel menandai ada item yang baru dihapus.
    LaunchedEffect(uiState.showUndoSnackbar) {
        if (uiState.showUndoSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "Jasa berhasil dihapus dari keranjang",
                actionLabel = "Urungkan",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoRemove()
            } else {
                onDismissUndoSnackbar()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { CartTopBar(itemCount = uiState.totalItemCount) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> CartLoadingContent()

                uiState.isError -> CartErrorContent(
                    message = uiState.errorMessage.orEmpty(),
                    onRetryClick = onRetryClick
                )

                uiState.isCartEmpty -> CartEmptyContent(onExploreServicesClick = onExploreServicesClick)

                else -> CartListContent(
                    uiState = uiState,
                    onToggleSelection = onToggleSelection,
                    onSelectAll = onSelectAll,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onDecreaseQuantity = onDecreaseQuantity,
                    onDeleteItemClick = onDeleteItemClick,
                    onEditItemClick = onEditItemClick,
                    onCheckoutClick = onCheckoutClick,
                    onViewVouchersClick = onViewVouchersClick
                )
            }

            if (uiState.showDeleteDialog && uiState.itemPendingDelete != null) {
                DeleteConfirmationDialog(
                    itemName = uiState.itemPendingDelete!!.serviceName,
                    onConfirm = onConfirmDelete,
                    onDismiss = onDismissDeleteDialog
                )
            }
        }
    }
}

// ============================================================
// SUB-COMPONENTS (STATLESS)
// ============================================================

@Composable
private fun DeleteConfirmationDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Jasa") },
        text = { Text("Apakah Anda yakin ingin menghapus \"$itemName\" dari keranjang?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Hapus", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun CartTopBar(itemCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Keranjang Saya",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = Primary100
            ) {
                Text(
                    text = "$itemCount Jasa",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary700,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CartLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = Primary700)
            Text(
                text = "Memuat keranjang...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CartErrorContent(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Gagal Memuat Keranjang",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(containerColor = Primary700)
        ) {
            Text("Coba Lagi")
        }
    }
}

@Composable
private fun CartEmptyContent(onExploreServicesClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Primary100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = Primary700
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Keranjang Kamu Masih Kosong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Temukan berbagai jasa mahasiswa terbaik untuk membantumu di kampus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onExploreServicesClick,
            colors = ButtonDefaults.buttonColors(containerColor = Secondary500),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Jelajahi Katalog Jasa", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CartListContent(
    uiState: CartUiState,
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onDeleteItemClick: (String) -> Unit,
    onEditItemClick: (String) -> Unit,
    onCheckoutClick: () -> Unit,
    onViewVouchersClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Pilih Semua
            item(key = "select_all_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onSelectAll)
                    ) {
                        Checkbox(
                            checked = uiState.isAllSelected,
                            onCheckedChange = { onSelectAll() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pilih Semua (${uiState.totalItemCount})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (uiState.selectedCount > 0) {
                        Text(
                            text = "${uiState.selectedCount} dipilih",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary700,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Daftar item keranjang
            items(uiState.items, key = { it.id }) { item ->
                CartItemCard(
                    item = item,
                    onSelectionChange = { onToggleSelection(item.id) },
                    onEditClick = { onEditItemClick(item.id) },
                    onDeleteClick = { onDeleteItemClick(item.id) },
                    onIncreaseQuantity = { onIncreaseQuantity(item.id) },
                    onDecreaseQuantity = { onDecreaseQuantity(item.id) }
                )
            }

            // Banner promo singkat di bawah keranjang
            item(key = "promo_banner_cart") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewVouchersClick),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary100.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gunakan Promo & Voucher",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary700
                            )
                            Text(
                                text = "${uiState.availableVouchers.size} voucher aktif tersedia untukmu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Bottom Bar Checkout
        CartBottomBar(
            selectedCount = uiState.selectedCount,
            total = uiState.subtotal,
            isCheckoutEnabled = uiState.isCheckoutEnabled,
            onCheckoutClick = onCheckoutClick
        )
    }
}

@Composable
private fun CartBottomBar(
    selectedCount: Int,
    total: Long,
    isCheckoutEnabled: Boolean,
    onCheckoutClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Total Harga ($selectedCount Jasa)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRupiah(total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }

            Button(
                onClick = onCheckoutClick,
                enabled = isCheckoutEnabled,
                modifier = Modifier
                    .height(48.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary500
                )
            ) {
                Text(
                    text = "Checkout",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
