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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class PromoDetailActions(
    val onClaimClick: () -> Unit,
    val onClaimSuccessDismiss: () -> Unit,
    val onDeleteClick: () -> Unit,
    val onDeleteConfirm: () -> Unit,
    val onDeleteDismiss: () -> Unit,
    val onMarkUsedClick: (String) -> Unit,
    val onMarkUsedConfirm: () -> Unit,
    val onMarkUsedDismiss: () -> Unit
)

@Composable
fun PromoDetailRoute(
    viewModel: PromoDetailViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenVouchers: () -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDeleted by rememberUpdatedState(onDeleted)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.Message -> snackbarHostState.showPromoMessage(event.text)
                DetailEvent.Deleted -> currentOnDeleted()
            }
        }
    }

    val actions = remember(viewModel) {
        PromoDetailActions(
            onClaimClick = viewModel::onClaimClick,
            onClaimSuccessDismiss = viewModel::onClaimSuccessDismiss,
            onDeleteClick = viewModel::onDeleteClick,
            onDeleteConfirm = viewModel::onDeleteConfirm,
            onDeleteDismiss = viewModel::onDeleteDismiss,
            onMarkUsedClick = viewModel::onMarkUsedClick,
            onMarkUsedConfirm = viewModel::onMarkUsedConfirm,
            onMarkUsedDismiss = viewModel::onMarkUsedDismiss
        )
    }

    PromoDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = actions,
        onBack = onBack,
        onEdit = onEdit,
        onOpenVouchers = onOpenVouchers
    )
}

@Composable
fun PromoDetailContent(
    state: PromoDetailUiState,
    snackbarHostState: SnackbarHostState,
    actions: PromoDetailActions,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenVouchers: () -> Unit
) {
    val promo = state.promo

    Scaffold(
        containerColor = PromoColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (promo != null && !state.isDeleting) {
                DetailBottomBar(state, promo, actions, onEdit, onOpenVouchers)
            }
        }
    ) { padding ->
        when {
            state.isLoading || state.isDeleting -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.isLoading) CircularProgressIndicator()
                }
            }

            promo == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Promo tidak ditemukan", color = PromoColors.TextMuted)
                        TextButton(onClick = onBack) { Text("Kembali") }
                    }
                }
            }

            else -> {
                DetailBody(
                    promo = promo,
                    state = state,
                    actions = actions,
                    onBack = onBack,
                    modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
                )
            }
        }
    }

    if (promo != null) {
        if (state.showClaimSuccess) {
            ClaimSuccessDialog(
                code = promo.code,
                onSeeVouchers = {
                    actions.onClaimSuccessDismiss()
                    onOpenVouchers()
                },
                onDismiss = actions.onClaimSuccessDismiss
            )
        }
        if (state.showDeleteDialog) {
            ConfirmDeleteDialog(
                promoTitle = promo.title,
                onConfirm = actions.onDeleteConfirm,
                onDismiss = actions.onDeleteDismiss
            )
        }
    }
    state.markTarget?.let { target ->
        MarkUsedDialog(
            userName = target.claim.userName,
            code = target.promoCode,
            onConfirm = actions.onMarkUsedConfirm,
            onDismiss = actions.onMarkUsedDismiss
        )
    }
}

@Composable
private fun DetailBottomBar(
    state: PromoDetailUiState,
    promo: Promo,
    actions: PromoDetailActions,
    onEdit: (String) -> Unit,
    onOpenVouchers: () -> Unit
) {
    Surface(shadowElevation = 12.dp, color = Color.White) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isOwner) {
                OutlinedButton(
                    onClick = { onEdit(promo.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = actions.onDeleteClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = BorderStroke(1.dp, PromoColors.Kedaluwarsa),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PromoColors.Kedaluwarsa
                    )
                ) {
                    Text("Hapus")
                }
            } else {
                when (state.claimButtonState) {
                    ClaimButtonState.CLAIMED -> OutlinedButton(
                        onClick = onOpenVouchers,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Lihat Voucher Saya")
                    }

                    ClaimButtonState.EXPIRED -> Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Promo Berakhir")
                    }

                    ClaimButtonState.FULL -> Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Kuota Habis")
                    }

                    ClaimButtonState.CLAIMABLE -> Button(
                        onClick = actions.onClaimClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
                    ) {
                        Text("Klaim Voucher", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBody(
    promo: Promo,
    state: PromoDetailUiState,
    actions: PromoDetailActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(listOf(PromoColors.Primary, PromoColors.PrimaryDark))
                )
        ) {
            Text(
                text = promo.emoji,
                fontSize = 80.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 196.dp)
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (promo.isExpired) {
                Text(
                    text = "Promo sudah berakhir pada ${formatTanggal(promo.endDate)}",
                    color = PromoColors.Kedaluwarsa,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFDECEC), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )
            }
            Text(
                text = promo.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = PromoColors.TextDark
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(PromoColors.SoftBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = promo.providerName.take(1),
                        color = PromoColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = promo.providerName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Penyedia Jasa", fontSize = 11.sp, color = PromoColors.TextMuted)
                }
                Text("⭐ 4,8", fontSize = 13.sp)
            }

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
                    text = promo.code,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PromoColors.Primary
                )
                TextButton(onClick = { copyCode(context, promo.code) }) { Text("Salin") }
            }

            InfoRow(
                Icons.Default.DateRange,
                "Periode berlaku: ${formatTanggal(promo.startDate)} - ${formatTanggal(promo.endDate)}"
            )
            InfoRow(Icons.Default.LocalOffer, "Potongan: ${discountText(promo)}")
            InfoRow(
                Icons.Default.ShoppingCart,
                "Minimal pesanan: " +
                        if (promo.minOrder > 0) formatRupiah(promo.minOrder) else "Tanpa minimum"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Sisa ${promo.remaining} dari ${promo.quota}",
                    fontSize = 12.sp,
                    color = PromoColors.TextMuted
                )
                QuotaBar(promo.claimed, promo.quota)
            }

            if (promo.description.isNotBlank()) {
                SectionTitle("Deskripsi")
                Text(promo.description, fontSize = 13.sp, color = PromoColors.TextMuted)
            }
            if (promo.terms.isNotEmpty()) {
                SectionTitle("Syarat & Ketentuan")
                promo.terms.forEachIndexed { index, term ->
                    Text(
                        text = "${index + 1}. $term",
                        fontSize = 13.sp,
                        color = PromoColors.TextMuted
                    )
                }
            }

            if (state.isOwner) {
                SectionTitle("Klaim Terbaru")
                if (state.providerClaims.isEmpty()) {
                    Text("Belum ada klaim.", fontSize = 12.sp, color = PromoColors.TextMuted)
                }
                state.providerClaims.forEach { item ->
                    ClaimRow(item = item, onMark = { actions.onMarkUsedClick(item.claim.id) })
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
                .background(Color.White, CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
        }

        Text(
            text = "${promo.badge} OFF",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PromoColors.TextDark,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 152.dp)
                .background(PromoColors.Sand, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

private fun discountText(promo: Promo): String =
    if (promo.discountType == DiscountType.PERSEN) {
        "${promo.discountValue}%" +
                if (promo.maxDiscount > 0) " (maks. ${formatRupiah(promo.maxDiscount)})" else ""
    } else {
        formatRupiah(promo.discountValue)
    }

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PromoColors.Primary,
            modifier = Modifier.size(18.dp)
        )
        Text(text, fontSize = 13.sp, color = PromoColors.TextDark)
    }
}