
package com.example.genksuruh.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.theme.Success
import com.example.genksuruh.feature.cart.components.OrderSummaryCard
import com.example.genksuruh.feature.cart.components.VoucherSection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun CheckoutScreen(
    viewModel: CartViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedItems = uiState.selectedItems

    val canPlaceOrder = selectedItems.isNotEmpty() &&
            !uiState.isLoading &&
            !uiState.isError &&
            !uiState.isCheckoutProcessing &&
            uiState.isWatuPayBalanceSufficient &&
            uiState.voucherStatus != VoucherStatus.VALIDATING

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CheckoutTopBar(onBackClick = onNavigateBack)
        },
        bottomBar = {
            CheckoutBottomBar(
                total = uiState.checkoutTotal,
                isEnabled = canPlaceOrder,
                isBalanceSufficient = uiState.isWatuPayBalanceSufficient,
                balanceShortfall = uiState.balanceShortfall,
                isProcessing = uiState.isCheckoutProcessing,
                onPlaceOrder = onPlaceOrder,
                onBackToCart = onNavigateBack
            )
        }
    ) { innerPadding ->

        if (selectedItems.isEmpty()) {
            EmptyCheckoutContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onBackClick = onNavigateBack
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = 2.dp,
                    end = 16.dp,
                    bottom = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                item(key = "checkout_trust_banner") {
                    CheckoutTrustBanner()
                }

                item(key = "checkout_order_title") {
                    SectionHeading(
                        title = "Ringkasan Pesanan",
                        trailing = "${selectedItems.size} Jasa",
                        icon = Icons.Filled.CheckCircle
                    )
                }

                item(key = "checkout_order_summary") {
                    OrderSummaryCard(items = selectedItems)
                }

                item(key = "checkout_execution_title") {
                    SectionHeading(
                        title = "Detail Pelaksanaan",
                        icon = Icons.Filled.LocationOn
                    )
                }

                item(key = "checkout_execution_details") {
                    CheckoutExecutionCard(items = selectedItems)
                }

                // VOUCHER
                item(key = "checkout_voucher") {
                    VoucherSection(
                        code = uiState.voucherCode,
                        status = uiState.voucherStatus,
                        message = uiState.voucherMessage,
                        discountPercent = uiState.voucherDiscountPercent,
                        minimumTransaction = uiState.voucherMinimumTransaction,
                        discountAmount = uiState.voucherDiscountAmount,
                        subtotal = uiState.subtotal,
                        onCodeChange = viewModel::updateVoucherCode,
                        onApplyClick = viewModel::applyVoucher,
                        onRemoveClick = viewModel::removeVoucher
                    )
                }

                // METODE PEMBAYARAN WATUPAY
                item(key = "checkout_payment_method") {
                    SectionHeading(
                        title = "Metode Pembayaran",
                        icon = Icons.Filled.AccountBalanceWallet
                    )

                    WatuPayMethodCard(
                        balance = uiState.watuPayBalance,
                        total = uiState.checkoutTotal
                    )
                }

                // RINCIAN PEMBAYARAN
                item(key = "checkout_payment_title") {
                    SectionHeading(
                        title = "Rincian Pembayaran",
                        icon = Icons.Filled.Security
                    )
                }

                item(key = "checkout_payment_summary") {
                    CheckoutPaymentSummary(
                        subtotal = uiState.subtotal,
                        discount = uiState.voucherDiscountAmount,
                        total = uiState.checkoutTotal
                    )
                }

                item(key = "checkout_security_note") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(17.dp)
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        Text(
                            text = "Pembayaran aman diproses setelah pesanan dikonfirmasi.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// TOP BAR
// ============================================================

@Composable
private fun CheckoutTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Kembali"
            )
        }

        Text(
            text = "Checkout",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profil",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ============================================================
// TRUST BANNER
// ============================================================

@Composable
private fun CheckoutTrustBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Primary100.copy(alpha = 0.75f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Primary700,
            modifier = Modifier.size(17.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = "GenkSuruh.co • Marketplace jasa mahasiswa UB",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Praktis & Cepat",
            style = MaterialTheme.typography.labelSmall,
            color = Success,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// SECTION HEADING
// ============================================================

@Composable
private fun SectionHeading(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (trailing != null) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// EXECUTION DETAILS
// ============================================================

@Composable
private fun CheckoutExecutionCard(
    items: List<CartItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEachIndexed { index, item ->

                if (items.size > 1) {
                    Text(
                        text = item.serviceName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary700
                    )
                }

                ExecutionDetailRow(
                    icon = Icons.Filled.LocationOn,
                    label = "Lokasi Pengantaran",
                    value = item.location
                )

                ExecutionDetailRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Waktu Pelaksanaan",
                    value = "${item.date} • ${item.time}"
                )

                if (item.note.isNotBlank()) {
                    ExecutionDetailRow(
                        icon = Icons.Filled.Info,
                        label = "Catatan Tambahan Pemesan",
                        value = item.note,
                        italic = true
                    )
                }

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.7f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ExecutionDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    italic: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Primary100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary700,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================================
// WATUPAY METHOD
// ============================================================


@Composable
private fun WatuPayMethodCard(
    balance: Long,
    total: Long
) {
    val shortfall = (total - balance).coerceAtLeast(0L)
    val isSufficient = shortfall == 0L

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (isSufficient) {
                                Primary100
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = "WatuPay",
                        tint = if (isSufficient) {
                            Primary700
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "WatuPay Saldo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Sisa Saldo: ${formatRupiah(balance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSufficient) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = if (isSufficient) {
                            FontWeight.Normal
                        } else {
                            FontWeight.SemiBold
                        }
                    )
                }
            }
        }

        // Peringatan saldo kurang berada di bawah WatuPay.
        // Tidak menggunakan banner merah besar di bagian atas.

        if (!isSufficient) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Selesaikan kekurangan dengan mengurangi jasa di keranjang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "Kurang ${formatRupiah(shortfall)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 22.dp)
                )
            }
        }
    }
}


// ============================================================
// PAYMENT SUMMARY
// ============================================================

@Composable
private fun CheckoutPaymentSummary(
    subtotal: Long,
    discount: Long,
    total: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PaymentSummaryRow(
                label = "Subtotal Pesanan",
                value = formatRupiah(subtotal)
            )

            PaymentSummaryRow(
                label = "Biaya Layanan Kampus",
                value = "Gratis",
                valueColor = Success
            )

            PaymentSummaryRow(
                label = "Diskon Voucher",
                value = if (discount > 0) {
                    "-${formatRupiah(discount)}"
                } else {
                    "Rp0 (Belum ada voucher)"
                },
                valueColor = if (discount > 0) {
                    Success
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formatRupiah(total),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }

            if (discount > 0) {
                Text(
                    text = "Hemat ${formatRupiah(discount)} dengan voucher!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PaymentSummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

// ============================================================
// BOTTOM ACTION
// ============================================================


@Composable
private fun CheckoutBottomBar(
    total: Long,
    isEnabled: Boolean,
    isBalanceSufficient: Boolean,
    balanceShortfall: Long,
    isProcessing: Boolean,
    onPlaceOrder: () -> Unit,
    onBackToCart: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "TOTAL PEMBAYARAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatRupiah(total),
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary700,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isBalanceSufficient) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Perlu tambahan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = "Kurang ${formatRupiah(balanceShortfall)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = onPlaceOrder,
                enabled = isEnabled && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary500
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(19.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text("Memproses Pesanan...")
                } else {
                    Text(
                        text = "Pesan Sekarang",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            if (!isBalanceSufficient) {
                OutlinedButton(
                    onClick = onBackToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Primary100,
                        contentColor = Primary700
                    ),
                    border = null
                ) {
                    Text(
                        text = "Kembali ke Keranjang (Sesuaikan Item)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


// ============================================================
// EMPTY CHECKOUT
// ============================================================

@Composable
private fun EmptyCheckoutContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Belum ada jasa untuk checkout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Pilih minimal satu jasa dari keranjang terlebih dahulu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackClick) {
            Text("Kembali ke Keranjang")
        }
    }
}
