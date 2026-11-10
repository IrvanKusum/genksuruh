
package com.example.genksuruh.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.theme.Success

// ============================================================
// MAIN SCREEN
// ============================================================

@Composable
fun CheckoutSuccessScreen(
    viewModel: CartViewModel = viewModel(),
    onNavigateToHistory: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val items = uiState.selectedItems
    val subtotal = uiState.subtotal
    val discount = uiState.voucherDiscountAmount
    val total = uiState.checkoutTotal

    val hasVoucher = discount > 0L
    val isMultipleItems = items.size > 1

    val orderId = when {
        hasVoucher -> "GS-ORD-20261101-003"
        isMultipleItems -> "GS-ORD-20261101-002"
        else -> "GS-ORD-20261101-001"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CheckoutSuccessTopBar(
                onClose = onNavigateHome
            )
        },
        bottomBar = {
            CheckoutSuccessBottomBar(
                onHistory = onNavigateToHistory,
                onHome = onNavigateHome
            )
        }
    ) { innerPadding ->

        if (items.isEmpty()) {
            EmptySuccessContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onHome = onNavigateHome
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // ICON SUKSES: SAMA UNTUK SEMUA VARIASI
                SuccessIllustration()

                Spacer(modifier = Modifier.height(12.dp))

                SuccessTitle(
                    itemCount = items.size,
                    hasVoucher = hasVoucher,
                    voucherCode = uiState.voucherCode,
                    discount = discount
                )

                Spacer(modifier = Modifier.height(20.dp))

                // KHUSUS VOUCHER BERHASIL DIGUNAKAN
                if (hasVoucher) {
                    UsedVoucherCard(
                        code = uiState.voucherCode,
                        discountLabel = uiState.voucherLabel,
                        discountAmount = discount
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // RINCIAN ORDER
                SuccessOrderCard(
                    orderId = orderId,
                    items = items,
                    subtotal = subtotal,
                    discount = discount,
                    total = total
                )

                Spacer(modifier = Modifier.height(14.dp))

                // WATUPAY
                WatuPaySuccessCard(
                    balance = uiState.watuPayBalance
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

// ============================================================
// TOP BAR
// ============================================================


@Composable
private fun CheckoutSuccessTopBar(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Tutup"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Checkout Success",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(34.dp)
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
// SUCCESS ILLUSTRATION
// ============================================================

/**
 * Ilustrasi keberhasilan yang sama untuk:
 * 1. Single item
 * 2. Multiple items
 * 3. Checkout dengan voucher
 *
 * Menggunakan lingkaran biru dengan check putih,
 * mengikuti desain Multiple Items dari Figma.
 */
@Composable
private fun SuccessIllustration() {
    Box(
        modifier = Modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        // Lingkaran luar
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Color(0xFFE4EAFE)),
            contentAlignment = Alignment.Center
        ) {
            // Lingkaran tengah
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC9D9FF)),
                contentAlignment = Alignment.Center
            ) {
                // Lingkaran biru utama
                Surface(
                    modifier = Modifier.size(68.dp),
                    shape = CircleShape,
                    color = Color(0xFF0755E9),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Pesanan berhasil",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }

        // Aksen dekoratif kecil
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 3.dp)
                .size(11.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF825F))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 3.dp, bottom = 5.dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF42EFB5))
        )
    }
}

// ============================================================
// SUCCESS TITLE
// ============================================================

@Composable
private fun SuccessTitle(
    itemCount: Int,
    hasVoucher: Boolean,
    voucherCode: String,
    discount: Long
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pesanan Berhasil!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(5.dp))

        val description = when {
            hasVoucher ->
                "Pesanan kamu berhasil dibuat dengan potongan voucher $voucherCode hemat ${formatRupiah(discount)}!"

            itemCount > 1 ->
                "$itemCount pesanan jasa kamu telah dibuat sekaligus. Penyedia jasa masing-masing akan segera menerima notifikasi."

            else ->
                "Pesanan kamu berhasil dibuat. Tunggu penyedia jasa menerima dan memproses pesananmu."
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================
// USED VOUCHER CARD
// ============================================================

@Composable
private fun UsedVoucherCard(
    code: String,
    discountLabel: String,
    discountAmount: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Success.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(23.dp),
                        tint = Success
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = discountLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = Color(0xFF00734B),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Terpakai",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Voucher berhasil digunakan pada pesanan ini",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Kamu berhasil menghemat ${formatRupiah(discountAmount)} pada transaksi ini!",
                style = MaterialTheme.typography.labelMedium,
                color = Success,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================
// ORDER DETAIL CARD
// ============================================================

@Composable
private fun SuccessOrderCard(
    orderId: String,
    items: List<CartItem>,
    subtotal: Long,
    discount: Long,
    total: Long
) {
    val firstItem = items.first()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // HEADER TRANSAKSI
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary100.copy(alpha = 0.45f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETAIL TRANSAKSI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = orderId,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }

                Text(
                    text = "${firstItem.date} • ${firstItem.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (items.size > 1) {
                    Text(
                        text = "${items.size} Jasa Dipesan",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary700,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // STATUS PESANAN
                Surface(
                    color = Color(0xFFFFF0E9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB83B13))
                            )

                            Spacer(modifier = Modifier.width(7.dp))

                            Text(
                                text = "Menunggu",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8C2D0C)
                            )
                        }

                        Text(
                            text = "Pesanan berhasil dibuat dan sedang menunggu konfirmasi penyedia jasa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9B3512)
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // DAFTAR JASA
                items.forEachIndexed { index, item ->
                    SuccessOrderItem(item = item)

                    if (index != items.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // RINCIAN PEMBAYARAN
                SuccessPriceRow(
                    label = if (items.size > 1) {
                        "Subtotal (${items.size} Jasa)"
                    } else {
                        "Subtotal"
                    },
                    value = formatRupiah(subtotal)
                )

                SuccessPriceRow(
                    label = "Biaya Layanan Kampus",
                    value = "Rp0 (Gratis)",
                    valueColor = Success
                )

                if (discount > 0) {
                    SuccessPriceRow(
                        label = "Diskon Voucher",
                        value = "-${formatRupiah(discount)}",
                        valueColor = Success
                    )
                }

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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
            }
        }
    }
}

// ============================================================
// SINGLE ORDER ITEM
// ============================================================

@Composable
private fun SuccessOrderItem(
    item: CartItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Primary100),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.serviceName,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(27.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = item.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Primary700,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.serviceName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "♙ ${item.providerName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Qty: ${item.quantity}x",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = item.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = formatRupiah(item.lineTotal),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================
// PRICE ROW
// ============================================================

@Composable
private fun SuccessPriceRow(
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
// WATUPAY SUCCESS
// ============================================================

@Composable
private fun WatuPaySuccessCard(
    balance: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary100.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Primary700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = "WatuPay",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Berhasil via WatuPay Saldo",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Sisa saldo: ${formatRupiah(balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Pembayaran berhasil",
                tint = Success,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

// ============================================================
// BOTTOM ACTION
// ============================================================

@Composable
private fun CheckoutSuccessBottomBar(
    onHistory: () -> Unit,
    onHome: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary500
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Lihat Riwayat Pesanan",
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Primary100,
                    contentColor = Primary700
                ),
                border = null
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Kembali ke Beranda",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================
// EMPTY SUCCESS FALLBACK
// ============================================================

@Composable
private fun EmptySuccessContent(
    modifier: Modifier = Modifier,
    onHome: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
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
            text = "Detail pesanan tidak tersedia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Data pesanan belum tersedia. Silakan kembali ke beranda.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onHome) {
            Text("Kembali ke Beranda")
        }
    }
}
