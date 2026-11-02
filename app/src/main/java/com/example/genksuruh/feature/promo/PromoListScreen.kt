package com.example.genksuruh.feature.promo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ConfirmationNumber
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
import kotlinx.coroutines.delay

// ============================================================
// USER: Promo Tersedia | Voucher Saya
// ============================================================

@Composable
fun PromoListScreen(vm: PromoViewModel, onOpenDetail: (String) -> Unit) {
    val promos by vm.promos.collectAsState()
    val claims by vm.claims.collectAsState()
    val tab = vm.listTab

    var category by remember { mutableStateOf("Semua") }
    var voucherFilter by remember { mutableStateOf<VoucherStatus?>(null) }
    var claimedCode by remember { mutableStateOf<String?>(null) }
    var claimError by remember { mutableStateOf<String?>(null) }
    var sheetPromo by remember { mutableStateOf<Promo?>(null) }

    val availablePromos = promos.filter {
        !it.isExpired && (category == "Semua" || it.category == category)
    }

    // Voucher terbaru di atas, lalu diurutkan Aktif -> Terpakai -> Kedaluwarsa
    val myVouchers = claims
        .filter { it.userName == PromoRepository.CURRENT_USER }
        .reversed()
        .mapNotNull { c ->
            promos.find { it.id == c.promoId }?.let { p -> Triple(c, p, vm.effectiveStatus(c, p)) }
        }
        .sortedBy { it.third.ordinal }
    val filteredVouchers = myVouchers.filter { voucherFilter == null || it.third == voucherFilter }

    Scaffold(
        containerColor = PromoColors.Background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PromoHeader(
                    title = "Promo & Voucher",
                    isProvider = vm.isProvider,
                    onToggleProvider = { vm.isProvider = it; vm.listTab = 0 }
                )
            }

            if (tab == 0) {
                item {
                    BannerPager(
                        banners = promos.filter { !it.isExpired && !it.isFull }.take(3),
                        onClick = onOpenDetail
                    )
                }
            }

            item {
                UnderlineTabs(
                    titles = listOf("Promo Tersedia", "Voucher Saya"),
                    selected = tab,
                    onSelect = { vm.listTab = it }
                )
            }

            if (tab == 0) {
                // ===== PROMO TERSEDIA =====
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(promoCategories) { c ->
                            ChipItem(c, c == category) { category = c }
                        }
                    }
                }
                if (availablePromos.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.Outlined.ConfirmationNumber,
                            "Belum ada promo tersedia",
                            "Pantau terus, promo baru segera hadir",
                            "Muat Ulang"
                        ) { category = "Semua" }
                    }
                }
                items(availablePromos, key = { it.id }) { p ->
                    val mine = claims.any {
                        it.promoId == p.id && it.userName == PromoRepository.CURRENT_USER
                    }
                    PromoCard(p, onClick = { onOpenDetail(p.id) }) {
                        when {
                            p.isFull -> SmallButton("Habis", enabled = false) {}
                            mine -> SmallButton("Diklaim", enabled = false) {}
                            else -> SmallButton("Klaim") {
                                val err = vm.claim(p.id)
                                if (err == null) claimedCode = p.code else claimError = err
                            }
                        }
                    }
                }
            } else {
                // ===== VOUCHER SAYA =====
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            ChipItem("Semua", voucherFilter == null) { voucherFilter = null }
                        }
                        items(VoucherStatus.values().toList()) { s ->
                            ChipItem(s.label, voucherFilter == s) { voucherFilter = s }
                        }
                    }
                }
                if (filteredVouchers.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.Outlined.ConfirmationNumber,
                            "Belum ada voucher",
                            "Klaim promo dulu, yuk!",
                            "Lihat Promo"
                        ) { vm.listTab = 0 }
                    }
                }
                items(filteredVouchers, key = { it.first.id }) { (c, p, status) ->
                    VoucherTicketCard(p, status, c.usedAt) { sheetPromo = p }
                }
            }
        }
    }

    claimedCode?.let { code ->
        ClaimSuccessDialog(
            code = code,
            onSeeVouchers = { vm.listTab = 1; claimedCode = null },
            onDismiss = { claimedCode = null }
        )
    }
    claimError?.let { msg -> ClaimFailedDialog(msg) { claimError = null } }
    sheetPromo?.let { VoucherSheet(it) { sheetPromo = null } }
}

// ============================================================
// PENYEDIA: Promo Saya
// ============================================================

@Composable
fun PromoManageScreen(
    vm: PromoViewModel,
    onOpenDetail: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit
) {
    val promos by vm.promos.collectAsState()
    val claims by vm.claims.collectAsState()

    var markTarget by remember { mutableStateOf<VoucherClaim?>(null) }
    var deleteTarget by remember { mutableStateOf<Promo?>(null) }

    val myPromos = promos.filter { it.ownerId == PromoRepository.CURRENT_PROVIDER_ID }
    val myPromoIds = myPromos.map { it.id }.toSet()
    val recentClaims = claims.filter { it.promoId in myPromoIds }.reversed()

    Scaffold(
        containerColor = PromoColors.Background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = PromoColors.Coral,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Buat Promo", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PromoHeader(
                    title = "Promo Saya",
                    isProvider = vm.isProvider,
                    onToggleProvider = { vm.isProvider = it; vm.listTab = 0 }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Promo Aktif", myPromos.count { !it.isExpired }, Modifier.weight(1f))
                    StatCard("Total Klaim", myPromos.sumOf { it.claimed }, Modifier.weight(1f))
                    StatCard(
                        "Terpakai",
                        recentClaims.count { it.status == VoucherStatus.TERPAKAI },
                        Modifier.weight(1f)
                    )
                }
            }

            item { SectionTitle("Promo Saya") }
            if (myPromos.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Outlined.ConfirmationNumber,
                        "Belum ada promo", "Buat promo pertamamu sekarang", null
                    ) {}
                }
            }
            items(myPromos, key = { it.id }) { p ->
                ProviderPromoCard(
                    promo = p,
                    onClick = { onOpenDetail(p.id) },
                    onEdit = { onEdit(p.id) },
                    onDelete = { deleteTarget = p }
                )
            }

            item { SectionTitle("Klaim Terbaru") }
            if (recentClaims.isEmpty()) {
                item { Text("Belum ada klaim.", fontSize = 12.sp, color = PromoColors.TextMuted) }
            }
            items(recentClaims, key = { it.id }) { c ->
                val p = promos.find { it.id == c.promoId }
                ClaimRow(c, vm.effectiveStatus(c, p)) { markTarget = c }
            }
            item { Spacer(Modifier.height(72.dp)) } // ruang untuk tombol mengambang
        }
    }

    markTarget?.let { c ->
        val code = promos.find { it.id == c.promoId }?.code ?: "-"
        MarkUsedDialog(
            c, code,
            onConfirm = { vm.markUsed(c.id); markTarget = null },
            onDismiss = { markTarget = null }
        )
    }
    deleteTarget?.let { p ->
        ConfirmDeleteDialog(
            p,
            onConfirm = { vm.deletePromo(p.id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

// ============================================================
// Banner carousel
// ============================================================

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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { page ->
            val p = banners[page]
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(PromoColors.Primary, PromoColors.PrimaryDark))
                    )
                    .clickable { onClick(p.id) }
                    .padding(16.dp)
            ) {
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.68f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        p.title, color = Color.White, fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        p.code, color = Color.White, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Text(p.emoji, fontSize = 56.sp, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(banners.size) { i ->
                val active = i == pagerState.currentPage
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