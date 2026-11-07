package com.example.genksuruh.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.theme.*
import kotlinx.coroutines.launch

/** Route: tracking_list  (Riwayat Pesanan / Pesanan Masuk) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    onOrderClick: (String) -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onExploreServices: () -> Unit = {},
    viewModel: TrackingViewModel = viewModel(),
) {
    val role = TrackingSession.role
    LaunchedEffect(role) { viewModel.setRole(role) }

    val tabs = OrderTab.values()
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TrackingTopBar(
                title = if (role == UserRole.USER) "Riwayat Pesanan" else "Pesanan Masuk",
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.Search, contentDescription = "Cari") }
                    IconButton(onClick = {}) { Icon(Icons.Filled.Tune, contentDescription = "Filter") }
                    // DEMO: ketuk avatar untuk berpindah User <-> Penyedia
                    AvatarButton(onClick = {
                        TrackingSession.role =
                            if (role == UserRole.USER) UserRole.PROVIDER else UserRole.USER
                    })
                },
            )
        },
        // padding sistem sudah ditangani Scaffold di MainActivity
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = androidx.compose.ui.graphics.Color.White,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(tab.title, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val tab = tabs[page]
                val state by viewModel.tabStates.getValue(tab).collectAsState()
                OrderListPage(
                    tab = tab,
                    state = state,
                    role = role,
                    onOrderClick = onOrderClick,
                    onChatClick = onChatClick,
                    onExploreServices = onExploreServices,
                )
            }
        }
    }
}

@Composable
private fun OrderListPage(
    tab: OrderTab,
    state: OrdersUiState,
    role: UserRole,
    onOrderClick: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onExploreServices: () -> Unit,
) {
    when (state) {
        OrdersUiState.Loading -> Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { repeat(4) { SkeletonCard() } }

        OrdersUiState.Empty -> {
            val (title, message) = when (tab) {
                OrderTab.BERJALAN -> "Belum ada pesanan" to (
                        if (role == UserRole.USER) "Semua pesanan layanan kamu yang sedang berjalan akan ditampilkan di sini."
                        else "Pesanan masuk dari pelanggan akan ditampilkan di sini."
                        )
                OrderTab.SELESAI -> "Belum ada pesanan selesai" to "Pesanan yang sudah selesai akan muncul di sini."
                OrderTab.DIBATALKAN -> "Tidak ada pesanan dibatalkan" to "Pesanan yang dibatalkan akan muncul di sini."
            }
            EmptyState(
                title = title,
                message = message,
                actionLabel = if (tab == OrderTab.BERJALAN && role == UserRole.USER) "Eksplor Layanan Sekarang" else null,
                onAction = onExploreServices,
            )
        }

        is OrdersUiState.Success -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (tab == OrderTab.BERJALAN) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Primary100)
                            .padding(16.dp),
                    ) {
                        Text("${state.orders.size} Layanan Aktif", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Update waktu nyata dari teknisi & mitra",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor(),
                        )
                    }
                }
            }
            items(state.orders, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    role = role,
                    onDetailClick = { onOrderClick(order.id) },
                    onChatClick = { onChatClick(order.id) },
                )
            }
        }
    }
}