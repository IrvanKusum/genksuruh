
package com.example.genksuruh.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.theme.Success

@Composable
fun CheckoutFailedScreen(
    viewModel: CartViewModel,
    onRetry: () -> Unit,
    onBackToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FailedTopBar(onBack = onBackToCheckout)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹ Status Transaksi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Bantuan",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary700,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ilustrasi error.
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFDCD8)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBE1919)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBE1919)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pesanan Gagal Dibuat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.checkoutFailureMessage
                    ?: "Maaf, terjadi kendala teknis saat memproses pesananmu. Data rincian tetap tersimpan aman.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Keamanan saldo WatuPay.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE2F0EE),
                shape = RoundedCornerShape(13.dp)
            ) {
                Row(
                    modifier = Modifier.padding(13.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color(0xFF087653),
                        modifier = Modifier.size(27.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Saldo WatuPay Aman",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF087653)
                        )

                        Text(
                            text = "Saldo WatuPay kamu TIDAK terpotong. Item keranjang dan promo voucher aktif tetap tersimpan di akunmu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF40565A),
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detail transaksi.
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
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS TRANSAKSI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Surface(
                            color = Color(0xFFFFDDDA),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Gagal Diproses",
                                modifier = Modifier.padding(
                                    horizontal = 9.dp,
                                    vertical = 5.dp
                                ),
                                color = Color(0xFFB71919),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Primary100,
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Primary700,
                                modifier = Modifier.size(17.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Pesanan belum berhasil dibuat ke sistem.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FailedDetailRow(
                        label = "Metode Pembayaran",
                        value = "WatuPay Saldo"
                    )

                    FailedDetailRow(
                        label = "Total Pembayaran",
                        value = formatRupiah(uiState.checkoutTotal),
                        valueColor = Primary700,
                        bold = true
                    )

                    FailedDetailRow(
                        label = "Waktu Percobaan",
                        value = uiState.checkoutAttemptAt
                            ?: "Tidak tersedia"
                    )

                    FailedDetailRow(
                        label = "Status Promo",
                        value = if (uiState.isVoucherApplied) {
                            "${uiState.voucherCode} Tersimpan"
                        } else {
                            "Tidak ada voucher"
                        },
                        valueColor = if (uiState.isVoucherApplied) {
                            Success
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (uiState.isVoucherApplied) {
                        Text(
                            text = "Voucher belum terpakai dan siap dipakai kembali.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Langkah penanganan.
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Secondary500,
                            modifier = Modifier.size(19.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Langkah Penanganan Cepat",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    RecoveryStep(
                        text = "Periksa kestabilan koneksi Wi-Fi atau data seluler kampusmu."
                    )

                    RecoveryStep(
                        text = "Ketuk Coba Lagi untuk mengirim ulang tanpa perlu menginput ulang data pesanan."
                    )

                    RecoveryStep(
                        text = "Jika kendala berlanjut, hubungi Bantuan Rekan Kampus GenkSuruh."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary500
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Coba Lagi",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBackToCheckout,
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
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Kembali ke Checkout",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Seluruh data pesanan, jadwal, dan catatan tetap tersimpan aman di keranjangmu.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun FailedTopBar(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .height(54.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Kembali"
            )
        }

        Text(
            text = "Checkout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FailedDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun RecoveryStep(
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Secondary500)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 19.sp
        )
    }
}
