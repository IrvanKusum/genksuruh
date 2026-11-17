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
import androidx.compose.ui.graphics.Color

@Composable
fun CheckoutScreen(
    viewModel: CartViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedItems = uiState.selectedItems
    val displayItems = selectedItems.ifEmpty { uiState.lastCheckoutSummary?.items ?: emptyList() }

    val canPlaceOrder = displayItems.isNotEmpty() &&
            !uiState.isLoading &&
            !uiState.isError &&
            !uiState.isCheckoutProcessing &&
            uiState.checkoutProcessStatus != CheckoutProcessStatus.SUCCESS &&
            uiState.isWatuPayBalanceSufficient &&
            uiState.voucherStatus != VoucherStatus.VALIDATING

    val showEmptyState = displayItems.isEmpty() &&
            uiState.checkoutProcessStatus != CheckoutProcessStatus.SUCCESS &&
            uiState.lastCheckoutSummary == null &&
            !uiState.isCheckoutProcessing

    val isProcessingOrSuccess = uiState.isCheckoutProcessing ||
            uiState.checkoutProcessStatus == CheckoutProcessStatus.SUCCESS

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
                isProcessing = isProcessingOrSuccess,
                onPlaceOrder = onPlaceOrder,
                onBackToCart = onNavigateBack
            )
        }
    ) { innerPadding ->

        if (isProcessingOrSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = Primary700)
                    Text(
                        text = "Memproses pesanan...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (showEmptyState) {
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
                        trailing = "${displayItems.size} Jasa",
                        icon = Icons.Filled.CheckCircle
                    )
                }

                item(key = "checkout_order_summary") {
                    OrderSummaryCard(items = displayItems)
                }

                item(key = "checkout_execution_title") {
                    SectionHeading(
                        title = "Detail Pelaksanaan",
                        icon = Icons.Filled.LocationOn
                    )
                }

                item(key = "checkout_execution_details") {
                    CheckoutExecutionCard(items = displayItems)
                }

                // VOUCHER
                item(key = "checkout_voucher") {
                    VoucherSection(
                        code = uiState.voucherCode,
                        status = uiState.voucherStatus,
                        message = uiState.voucherMessage,
                        discountLabel = uiState.voucherLabel,
                        minimumTransaction = uiState.voucherMinimumTransaction,
                        discountAmount = uiState.voucherDiscountAmount,
                        subtotal = uiState.subtotal,
                        availableVouchers = uiState.availableVouchers,
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
                    PaymentSummaryCard(
                        subtotal = uiState.subtotal,
                        discount = uiState.voucherDiscountAmount,
                        total = uiState.checkoutTotal
                    )
                }

                item(key = "checkout_security_note") {
                    SecurityNote()
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
                contentDescription = "Kembali",
                tint = MaterialTheme.colorScheme.onSurface
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary100.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Primary700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Transaksi Terjamin Aman",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )

                Text(
                    text = "Dana diteruskan ke penyedia setelah tugas selesai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// SECTION HEADING
// ============================================================

@Composable
private fun SectionHeading(
    title: String,
    trailing: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary700,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (!trailing.isNullOrBlank()) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = Primary700,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================
// EXECUTION DETAILS CARD
// ============================================================

@Composable
private fun CheckoutExecutionCard(
    items: List<CartItem>
) {
    val first = items.firstOrNull() ?: return

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "Lokasi Pelaksanaan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = first.location,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "Waktu Pelaksanaan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${first.date} • ${first.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (items.size > 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "+ ${items.size - 1} jasa lainnya dengan jadwal dan lokasi masing-masing.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary700,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================
// WATUPAY PAYMENT METHOD CARD
// ============================================================

@Composable
private fun WatuPayMethodCard(
    balance: Long,
    total: Long
) {
    val isSufficient = balance >= total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Primary100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "WatuPay Saldo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Saldo tersedia: ${formatRupiah(balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSufficient) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Success
            ) {
                Text(
                    text = "Utama",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================
// PAYMENT SUMMARY CARD
// ============================================================

@Composable
private fun PaymentSummaryCard(
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CheckoutPriceRow(label = "Subtotal Jasa", value = formatRupiah(subtotal))
            CheckoutPriceRow(label = "Biaya Layanan Kampus", value = "Rp0 (Gratis)", valueColor = Success)

            if (discount > 0) {
                CheckoutPriceRow(
                    label = "Diskon Voucher",
                    value = "-${formatRupiah(discount)}",
                    valueColor = Success
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }
        }
    }
}

@Composable
private fun CheckoutPriceRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

// ============================================================
// SECURITY NOTE
// ============================================================

@Composable
private fun SecurityNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Pembayaran aman dan terenkripsi secara end-to-end",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================
// BOTTOM BAR
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isBalanceSufficient) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saldo kurang ${formatRupiah(balanceShortfall)}. Silakan top up WatuPay.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text(
                        text = "Total Tagihan",
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
                    onClick = onPlaceOrder,
                    enabled = isEnabled && !isProcessing,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary500
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Memproses...",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Pesan Sekarang",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// EMPTY CHECKOUT CONTENT
// ============================================================

@Composable
private fun EmptyCheckoutContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Belum ada jasa untuk checkout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pilih jasa terlebih dahulu dari keranjang atau katalog.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackClick) {
            Text("Kembali ke Keranjang")
        }
    }
}
