package com.example.genksuruh.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.theme.Danger
import com.example.genksuruh.core.theme.GenkSuruhTheme
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.feature.cart.components.CartItemCard
import com.example.genksuruh.feature.cart.components.CartSummaryCard
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Surface
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.theme.Success
import androidx.compose.foundation.layout.offset

/**
 * Entry point berstatus (terhubung ke [CartViewModel]) untuk route CART_LIST.
 * Hanya bertugas mengumpulkan state & meneruskan aksi -- semua tampilan ada di [CartScreenContent].
 *
 * `onNavigateToItemDetail` dan `onNavigateToCheckout` sengaja disiapkan sebagai callback
 * kosong (TODO) karena route CART_ITEM_DETAIL dan CHECKOUT, serta layar tujuannya, belum
 * ada di NavGraph pada tahap ini. Setelah layar tersebut dibuat, NavGraph cukup mengisi
 * kedua callback ini dengan `navController.navigate(...)`.
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
        onViewVouchersClick = onNavigateToPromo
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
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                CartDeleteDialog(
                    serviceName = uiState.itemPendingDelete.serviceName,
                    onConfirm = onConfirmDelete,
                    onDismiss = onDismissDeleteDialog
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
private fun CartTopBar(itemCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Keranjang Saya",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "$itemCount jasa di keranjang",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { /* Notifikasi */ }) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifikasi"
            )
        }

        IconButton(onClick = { /* Profil */ }) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profil"
            )
        }
    }
}


/** FRAME 1 / 2 / 3 / 4 — daftar item, select all, dan ringkasan harga. */

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
        modifier = Modifier.fillMaxSize()
    ) {

        // ==========================================
        // PILIH SEMUA
        // ==========================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isAllSelected,
                onCheckedChange = { onSelectAll() }
            )

            Text(
                text = "Pilih Semua",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${uiState.selectedCount} dari ${uiState.totalItemCount} jasa dipilih",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ==========================================
        // DAFTAR ITEM DAN BANNER
        // ==========================================

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Daftar jasa.
            items(
                items = uiState.items,
                key = { it.id }
            ) { item ->

                CartItemCard(
                    item = item,
                    onSelectionChange = {
                        onToggleSelection(item.id)
                    },
                    onEditClick = {
                        onEditItemClick(item.id)
                    },
                    onDeleteClick = {
                        onDeleteItemClick(item.id)
                    },
                    onIncreaseQuantity = {
                        onIncreaseQuantity(item.id)
                    },
                    onDecreaseQuantity = {
                        onDecreaseQuantity(item.id)
                    }
                )
            }

            // ======================================
            // INFORMASI KEAMANAN DANA
            // ======================================

            item(key = "cart_safety_banner") {
                CartSafetyBanner()
            }

            // ======================================
            // DISKON DAN VOUCHER
            // ======================================

            item(key = "cart_voucher_banner") {
                CartVoucherBanner(
                    onClick = onViewVouchersClick
                )
            }
        }

        // ==========================================
        // RINGKASAN CHECKOUT (TETAP DI BAWAH)
        // ==========================================

        CartSummaryCard(
            selectedCount = uiState.selectedCount,
            total = uiState.total,
            isCheckoutEnabled = uiState.isCheckoutEnabled,
            onCheckoutClick = onCheckoutClick,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 8.dp
                )
                .offset(y = 12.dp)
        )
    }
}

// ==================================================
// BANNER KEAMANAN DANA
// ==================================================

@Composable
private fun CartSafetyBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary100.copy(alpha = 0.75f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF42EFB5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = Color(0xFF087653),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = "Dana ditahan aman sampai tugas selesai dikonfirmasi olehmu. Tanpa biaya admin tambahan!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

// ==================================================
// BANNER DISKON DAN VOUCHER
// ==================================================

@Composable
private fun CartVoucherBanner(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color(0xFFFFE9E5),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 9.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalOffer,
                contentDescription = null,
                tint = Secondary500,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "Diskon & voucher dapat dipakai di checkout",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF71351E)
            )

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Lihat voucher",
                tint = Color(0xFF71351E),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/** FRAME 3 — Empty State. */
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
                .size(96.dp)
                .clip(CircleShape)
                .background(Primary100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCartCheckout,
                contentDescription = null,
                tint = Primary700,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Keranjang kamu masih kosong",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Yuk, temukan jasa yang bisa membantu kebutuhanmu hari ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onExploreServicesClick,
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Jelajahi Jasa")
        }
    }
}

/** FRAME 5 — Error State. */
@Composable
private fun CartErrorContent(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Danger.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = null,
                tint = Danger,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Gagal Memuat Keranjang",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Coba Lagi")
        }
    }
}

/** FRAME 8 — Loading State: skeleton placeholder, tidak menampilkan dummy data seolah selesai dimuat. */
@Composable
private fun CartLoadingContent() {
    val skeletonColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkeletonBox(width = 64.dp, height = 64.dp, color = skeletonColor, shape = RoundedCornerShape(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBox(width = 160.dp, height = 16.dp, color = skeletonColor)
                        SkeletonBox(width = 100.dp, height = 12.dp, color = skeletonColor)
                        SkeletonBox(width = 80.dp, height = 12.dp, color = skeletonColor)
                    }
                }
                SkeletonBox(width = Dp.Unspecified, height = 40.dp, color = skeletonColor, fillWidth = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonBox(width = 70.dp, height = 20.dp, color = skeletonColor)
                    SkeletonBox(width = 90.dp, height = 28.dp, color = skeletonColor, shape = RoundedCornerShape(50))
                }
            }
        }
        SkeletonBox(width = Dp.Unspecified, height = 120.dp, color = skeletonColor, fillWidth = true, shape = RoundedCornerShape(16.dp))
    }
}

@Composable
private fun SkeletonBox(
    width: Dp,
    height: Dp,
    color: Color,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    fillWidth: Boolean = false
) {
    Box(
        modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier.width(width))
            .height(height)
            .clip(shape)
            .background(color)
    )
}

/** FRAME 6 — Dialog konfirmasi hapus item. */
@Composable
private fun CartDeleteDialog(
    serviceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Danger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Danger)
            }
        },
        title = { Text(text = "Hapus jasa ini?", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Text(
                text = "$serviceName akan dihapus dari keranjang.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Danger)
            ) {
                Text(text = "Ya, Hapus")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

// ============================= PREVIEWS =============================
// Kedelapan Preview memakai satu CartScreenContent yang sama, hanya beda UiState.

@Preview(showBackground = true, name = "1. Cart Default")
@Composable
private fun CartDefaultPreview() {
    GenkSuruhTheme {
        CartScreenContent(
            uiState = CartUiState(isLoading = false, items = CartDummyData.defaultItems()),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "2. Partial Selection")
@Composable
private fun CartPartialSelectionPreview() {
    GenkSuruhTheme {
        CartScreenContent(
            uiState = CartUiState(isLoading = false, items = CartDummyData.partialSelectionItems()),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "3. Empty State")
@Composable
private fun CartEmptyPreview() {
    GenkSuruhTheme {
        CartScreenContent(
            uiState = CartUiState(isLoading = false, items = emptyList()),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "4. No Selection")
@Composable
private fun CartNoSelectionPreview() {
    GenkSuruhTheme {
        CartScreenContent(
            uiState = CartUiState(isLoading = false, items = CartDummyData.noSelectionItems()),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "5. Error State")
@Composable
private fun CartErrorPreview() {
    GenkSuruhTheme {
        CartScreenContent(
            uiState = CartUiState(
                isLoading = false,
                isError = true,
                errorMessage = CartDummyData.GENERIC_ERROR_MESSAGE
            ),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "6. Delete Dialog")
@Composable
private fun CartDeleteDialogPreview() {
    GenkSuruhTheme {
        val items = CartDummyData.defaultItems()
        CartScreenContent(
            uiState = CartUiState(
                isLoading = false,
                items = items,
                itemPendingDelete = items.first(),
                showDeleteDialog = true
            ),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "7. Item Removed (Undo)")
@Composable
private fun CartItemRemovedPreview() {
    GenkSuruhTheme {
        val items = CartDummyData.defaultItems()
        CartScreenContent(
            uiState = CartUiState(
                isLoading = false,
                items = items.drop(1),
                lastRemovedItem = items.first(),
                lastRemovedItemIndex = 0,
                showUndoSnackbar = true
            ),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}

@Preview(showBackground = true, name = "8. Loading State")
@Composable
private fun CartLoadingPreview() {
    GenkSuruhTheme {
        CartScreenContent(
            uiState = CartUiState(isLoading = true),
            onToggleSelection = {}, onSelectAll = {}, onIncreaseQuantity = {}, onDecreaseQuantity = {},
            onDeleteItemClick = {}, onConfirmDelete = {}, onDismissDeleteDialog = {}, onUndoRemove = {},
            onDismissUndoSnackbar = {}, onRetryClick = {}, onEditItemClick = {}, onCheckoutClick = {},
            onExploreServicesClick = {}
        )
    }
}