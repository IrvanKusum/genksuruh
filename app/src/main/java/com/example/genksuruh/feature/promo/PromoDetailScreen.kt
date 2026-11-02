package com.example.genksuruh.feature.promo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PromoDetailScreen(
    promoId: String,
    vm: PromoViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenVouchers: () -> Unit
) {
    val promos by vm.promos.collectAsState()
    val claims by vm.claims.collectAsState()
    val context = LocalContext.current
    var showClaimed by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var claimError by remember { mutableStateOf<String?>(null) }

    val promo = promos.find { it.id == promoId }
    if (promo == null) {
        // promo sudah dihapus / id tidak ditemukan -> kembali
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val isOwner = vm.isProvider && promo.ownerId == PromoRepository.CURRENT_PROVIDER_ID
    val myClaim = claims.find {
        it.promoId == promoId && it.userName == PromoRepository.CURRENT_USER
    }

    Scaffold(
        containerColor = PromoColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val disabledColors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFFD5DDE3),
                        disabledContentColor = PromoColors.TextMuted
                    )
                    if (isOwner) {
                        OutlinedButton(
                            onClick = { onEdit(promo.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PromoColors.Primary)
                        ) { Text("Edit", fontWeight = FontWeight.SemiBold) }
                        OutlinedButton(
                            onClick = { showDelete = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PromoColors.Kedaluwarsa),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PromoColors.Kedaluwarsa
                            )
                        ) { Text("Hapus", fontWeight = FontWeight.SemiBold) }
                    } else {
                        when {
                            myClaim != null -> OutlinedButton(
                                onClick = onOpenVouchers,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, PromoColors.Primary)
                            ) { Text("Lihat Voucher Saya", fontWeight = FontWeight.SemiBold) }

                            promo.isExpired -> Button(
                                onClick = {}, enabled = false, colors = disabledColors,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) { Text("Promo Berakhir", fontWeight = FontWeight.SemiBold) }

                            promo.isFull -> Button(
                                onClick = {}, enabled = false, colors = disabledColors,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) { Text("Kuota Habis", fontWeight = FontWeight.SemiBold) }

                            else -> Button(
                                onClick = {
                                    val err = vm.claim(promo.id)
                                    if (err == null) showClaimed = true else claimError = err
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
                            ) { Text("Klaim Voucher", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(Modifier.fillMaxWidth()) {
                    // Hero
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(PromoColors.Primary, PromoColors.PrimaryDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(promo.emoji, fontSize = 80.sp)
                    }

                    // Kartu konten menimpa bagian bawah hero
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 176.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (promo.isExpired) {
                            Text(
                                "Promo sudah berakhir pada ${formatTanggal(promo.endDate)}",
                                color = PromoColors.Kedaluwarsa, fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFDECEC), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            )
                        }
                        Text(
                            promo.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                            color = PromoColors.TextDark
                        )

                        // Penyedia
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(PromoColors.SoftBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    promo.providerName.take(1), color = PromoColors.Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                promo.providerName, fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = PromoColors.TextDark,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.Star, contentDescription = null,
                                tint = PromoColors.Sand, modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("4,8", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Kotak kode
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(PromoColors.SoftBlue, RoundedCornerShape(12.dp))
                                .dashedBorder(PromoColors.Primary)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                promo.code, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                color = PromoColors.Primary
                            )
                            TextButton(onClick = { copyCode(context, promo.code) }) { Text("Salin") }
                        }

                        InfoRow(
                            Icons.Outlined.DateRange,
                            "Periode berlaku: ${formatPeriode(promo.startDate, promo.endDate)}"
                        )
                        InfoRow(
                            Icons.Outlined.LocalOffer,
                            "Potongan: " + if (promo.discountType == DiscountType.PERSEN) {
                                "${promo.discountValue}%" +
                                        if (promo.maxDiscount > 0) " (maks. ${formatRupiah(promo.maxDiscount)})" else ""
                            } else formatRupiah(promo.discountValue)
                        )
                        InfoRow(
                            Icons.Outlined.ShoppingCart,
                            "Minimal pesanan: " +
                                    if (promo.minOrder > 0) formatRupiah(promo.minOrder) else "Tanpa minimum"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Sisa ${promo.remaining} dari ${promo.quota}",
                                fontSize = 12.sp, color = PromoColors.TextMuted
                            )
                            QuotaBar(promo.claimed, promo.quota)
                        }

                        if (promo.terms.isNotEmpty()) {
                            SectionTitle("Syarat & Ketentuan")
                            promo.terms.forEachIndexed { i, t ->
                                Text("${i + 1}. $t", fontSize = 13.sp, color = PromoColors.TextMuted)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Badge kuning di kiri bawah gambar
                    Text(
                        "${promo.badge} OFF", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = PromoColors.TextDark,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 140.dp)
                            .background(PromoColors.Sand, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Tombol back bulat putih
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") }
        }
    }

    if (showClaimed) {
        ClaimSuccessDialog(
            code = promo.code,
            onSeeVouchers = { showClaimed = false; onOpenVouchers() },
            onDismiss = {
                showClaimed = false
                vm.showMessage("Voucher berhasil diklaim", success = true)
            }
        )
    }
    claimError?.let { msg -> ClaimFailedDialog(msg) { claimError = null } }
    if (showDelete) {
        ConfirmDeleteDialog(
            promo,
            onConfirm = {
                showDelete = false
                vm.deletePromo(promo.id) // layar otomatis kembali karena promo hilang
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PromoColors.Primary, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 13.sp, color = PromoColors.TextDark)
    }
}