package com.example.genksuruh.feature.promo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

data class PromoListActions(
    val onProviderModeChange: (Boolean) -> Unit,
    val onTabSelected: (Int) -> Unit,
    val onCategorySelected: (String) -> Unit,
    val onVoucherFilterSelected: (VoucherStatus?) -> Unit,
    val onClaimClick: (String) -> Unit,
    val onClaimSuccessDismiss: () -> Unit,
    val onSeeMyVouchers: () -> Unit,
    val onUseVoucherClick: (String) -> Unit,
    val onVoucherSheetDismiss: () -> Unit,
    val onMarkUsedClick: (String) -> Unit,
    val onMarkUsedConfirm: () -> Unit,
    val onMarkUsedDismiss: () -> Unit,
    val onDeleteClick: (String) -> Unit,
    val onDeleteConfirm: () -> Unit,
    val onDeleteDismiss: () -> Unit
)

/** Penghubung ViewModel dan tampilan. */
@Composable
fun PromoListRoute(
    viewModel: PromoListViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenDetail: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            snackbarHostState.showPromoMessage(event.message, event.undoable) {
                viewModel.onUndoDelete()
            }
        }
    }

    val actions = remember(viewModel) {
        PromoListActions(
            onProviderModeChange = viewModel::onProviderModeChange,
            onTabSelected = viewModel::onTabSelected,
            onCategorySelected = viewModel::onCategorySelected,
            onVoucherFilterSelected = viewModel::onVoucherFilterSelected,
            onClaimClick = viewModel::onClaimClick,
            onClaimSuccessDismiss = viewModel::onClaimSuccessDismiss,
            onSeeMyVouchers = viewModel::onSeeMyVouchers,
            onUseVoucherClick = viewModel::onUseVoucherClick,
            onVoucherSheetDismiss = viewModel::onVoucherSheetDismiss,
            onMarkUsedClick = viewModel::onMarkUsedClick,
            onMarkUsedConfirm = viewModel::onMarkUsedConfirm,
            onMarkUsedDismiss = viewModel::onMarkUsedDismiss,
            onDeleteClick = viewModel::onDeleteClick,
            onDeleteConfirm = viewModel::onDeleteConfirm,
            onDeleteDismiss = viewModel::onDeleteDismiss
        )
    }

    PromoListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = actions,
        onOpenDetail = onOpenDetail,
        onCreate = onCreate,
        onEdit = onEdit,
        onOpenProfile = onOpenProfile
    )
}

@Composable
fun PromoListContent(
    state: PromoListUiState,
    snackbarHostState: SnackbarHostState,
    actions: PromoListActions,
    onOpenDetail: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    Scaffold(
        containerColor = PromoColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isProvider && state.selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = onCreate,
                    containerColor = PromoColors.Coral,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Buat Promo", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ListHeader(state, onOpenProfile) }
                item { BannerPager(state.banners, onOpenDetail) }
                item {
                    SimpleTabs(
                        titles = listOf(
                            "Promo Tersedia",
                            if (state.isProvider) "Promo Saya" else "Voucher Saya"
                        ),
                        selected = state.selectedTab,
                        onSelect = actions.onTabSelected
                    )
                }
                when {
                    state.selectedTab == 0 -> availableSection(state, actions, onOpenDetail)
                    !state.isProvider -> voucherSection(state, actions)
                    else -> providerSection(state, actions, onOpenDetail, onEdit)
                }
            }
        }
    }

    state.claimSuccessCode?.let { code ->
        ClaimSuccessDialog(
            code = code,
            onSeeVouchers = actions.onSeeMyVouchers,
            onDismiss = actions.onClaimSuccessDismiss
        )
    }
    state.voucherSheet?.let { promo ->
        VoucherSheet(promo = promo, onDismiss = actions.onVoucherSheetDismiss)
    }
    state.markTarget?.let { target ->
        MarkUsedDialog(
            userName = target.claim.userName,
            code = target.promoCode,
            onConfirm = actions.onMarkUsedConfirm,
            onDismiss = actions.onMarkUsedDismiss
        )
    }
    state.deleteTarget?.let { promo ->
        ConfirmDeleteDialog(
            promoTitle = promo.title,
            onConfirm = actions.onDeleteConfirm,
            onDismiss = actions.onDeleteDismiss
        )
    }
}

@Composable
private fun ListHeader(state: PromoListUiState, onOpenProfile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Promo & Voucher",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = PromoColors.TextDark
            )
            Text(
                text = if (state.isProvider) "Mode Penyedia: ${state.providerName}"
                else "Halo, ${state.userName} 👋",
                fontSize = 12.sp,
                color = PromoColors.TextMuted
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Cari dan filter: baru tampilan, aksinya menyusul.
            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "Cari", tint = PromoColors.TextDark)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Tune, contentDescription = "Filter", tint = PromoColors.TextDark)
            }
            Spacer(Modifier.width(2.dp))
            // Tombol profil: membuka layar profil.
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PromoColors.Primary)
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun LazyListScope.availableSection(
    state: PromoListUiState,
    actions: PromoListActions,
    onOpenDetail: (String) -> Unit
) {
    item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(promoCategories) { category ->
                ChipItem(
                    label = category,
                    selected = category == state.selectedCategory,
                    onClick = { actions.onCategorySelected(category) }
                )
            }
        }
    }
    if (state.availablePromos.isEmpty()) {
        item {
            EmptyState(
                emoji = "🎟️",
                title = "Belum ada promo tersedia",
                subtitle = "Pantau terus, promo baru segera hadir",
                buttonLabel = null,
                onClick = {}
            )
        }
    }
    items(state.availablePromos, key = { it.promo.id }) { entry ->
        PromoCard(
            promo = entry.promo,
            onClick = { onOpenDetail(entry.promo.id) }
        ) {
            when {
                entry.promo.isFull -> SmallButton("Habis", enabled = false) {}
                entry.claimedByMe -> SmallButton("Diklaim", enabled = false) {}
                else -> SmallButton("Klaim") { actions.onClaimClick(entry.promo.id) }
            }
        }
    }
}

private fun LazyListScope.voucherSection(
    state: PromoListUiState,
    actions: PromoListActions
) {
    item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ChipItem(
                    label = "Semua (${state.totalVouchers})",
                    selected = state.voucherFilter == null,
                    onClick = { actions.onVoucherFilterSelected(null) }
                )
            }
            items(VoucherStatus.values().toList()) { status ->
                ChipItem(
                    label = status.label,
                    selected = state.voucherFilter == status,
                    onClick = { actions.onVoucherFilterSelected(status) }
                )
            }
        }
    }
    if (state.vouchers.isEmpty()) {
        item {
            EmptyState(
                emoji = "🎫",
                title = "Belum ada voucher",
                subtitle = "Klaim promo dulu, yuk!",
                buttonLabel = "Lihat Promo",
                onClick = { actions.onTabSelected(0) }
            )
        }
    }
    items(state.vouchers, key = { it.claim.id }) { entry ->
        VoucherTicketCard(
            promo = entry.promo,
            status = entry.status,
            usedAt = entry.claim.usedAt,
            onUse = { actions.onUseVoucherClick(entry.promo.id) }
        )
    }
}

private fun LazyListScope.providerSection(
    state: PromoListUiState,
    actions: PromoListActions,
    onOpenDetail: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Promo Aktif", state.activePromoCount, Modifier.weight(1f))
            StatCard("Total Klaim", state.totalClaimCount, Modifier.weight(1f))
            StatCard("Terpakai", state.usedClaimCount, Modifier.weight(1f))
        }
    }
    item { SectionTitle("Promo Saya") }
    if (state.myPromos.isEmpty()) {
        item {
            EmptyState(
                emoji = "📣",
                title = "Belum ada promo",
                subtitle = "Buat promo pertamamu sekarang",
                buttonLabel = null,
                onClick = {}
            )
        }
    }
    items(state.myPromos, key = { it.id }) { promo ->
        PromoCard(promo = promo, onClick = { onOpenDetail(promo.id) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(
                    if (promo.isExpired) VoucherStatus.KEDALUWARSA else VoucherStatus.AKTIF
                )
                OverflowMenu(
                    onEdit = { onEdit(promo.id) },
                    onDelete = { actions.onDeleteClick(promo.id) }
                )
            }
        }
    }
    item { SectionTitle("Klaim Terbaru") }
    if (state.providerClaims.isEmpty()) {
        item {
            Text("Belum ada klaim.", fontSize = 12.sp, color = PromoColors.TextMuted)
        }
    }
    items(state.providerClaims, key = { it.claim.id }) { entry ->
        ClaimRow(item = entry, onMark = { actions.onMarkUsedClick(entry.claim.id) })
    }
    item { Spacer(Modifier.height(72.dp)) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannerPager(banners: List<Promo>, onClick: (String) -> Unit) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(banners.size) {
        while (banners.size > 1) {
            delay(4000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
        }
    }

    val gradients = listOf(
        listOf(PromoColors.Primary, PromoColors.PrimaryDark),
        listOf(PromoColors.Coral, Color(0xFFE85D3A)),
        listOf(PromoColors.PrimaryDark, PromoColors.Primary)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { page ->
            val promo = banners[page]
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(gradients[page % gradients.size]))
                    .clickable { onClick(promo.id) }
                    .padding(16.dp)
            ) {
                Text(
                    text = "PROMO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PromoColors.TextDark,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(PromoColors.Sand, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.7f)
                ) {
                    Text(
                        text = promo.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kode: ${promo.code}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = promo.emoji,
                    fontSize = 56.sp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(banners.size) { index ->
                val active = index == pagerState.currentPage
                Box(
                    Modifier
                        .height(6.dp)
                        .width(if (active) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (active) PromoColors.Primary else Color(0xFFBFD5E0))
                )
            }
        }
    }
}