
package com.example.genksuruh.feature.tracking

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.theme.*
import kotlinx.coroutines.launch

/** Route: tracking_detail/{orderId} */
@Composable
fun TrackingDetailScreen(
    onBack: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onReview: (String) -> Unit = {},
    onReorder: (String) -> Unit = {},
    onHelp: () -> Unit = {},
    onChat: (String) -> Unit = {},
    showStatusUpdatedMessage: Boolean = false,
    onStatusUpdatedMessageShown: () -> Unit = {},
    viewModel: TrackingDetailViewModel = viewModel(),
) {
    val role = TrackingSession.role
    val detail by viewModel.detail.collectAsState()
    val ownReview by viewModel.ownReview.collectAsState()

    val hasReview = ownReview != null

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showStatusUpdatedMessage) {
        if (showStatusUpdatedMessage) {
            snackbarHostState.showSnackbar(
                "Status pesanan berhasil diperbarui"
            )
            onStatusUpdatedMessageShown()
        }
    }

    val d = detail

    Scaffold(
        topBar = {
            TrackingTopBar("Detail Pesanan", onBack = onBack)
        },
        bottomBar = {
            if (d != null) {
                DetailActionBar(
                    role = role,
                    status = d.order.status,
                    hasReview = hasReview,
                    onCancel = {
                        showCancelDialog = true
                    },
                    onReview = {
                        onReview(d.order.id)
                    },
                    onReorder = {
                        onReorder(d.order.id)
                    },
                    onHelp = onHelp,
                    onUpdateStatus = {
                        onUpdateStatus(d.order.id)
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        if (d == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val order = d.order

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // HEADER STATUS PESANAN
            StatusHeaderCard(
                order = order,
                role = role,
                hasReview = hasReview
            )

            // INFORMASI PENGGUNA / PENYEDIA
            if (role == UserRole.USER) {
                PersonInfoCard(
                    roleLabel = "Penyedia Jasa",
                    name = order.providerName,
                    subtitle = order.providerCompany,
                    extra = "★ ${order.providerRating} • ${order.providerOrderInfo}",
                    chatLabel = "Chat Mitra",
                    onChat = { onChat(order.id) },
                    onCall = {
                        context.dial(order.providerPhone)
                    },
                )
            } else {
                PersonInfoCard(
                    roleLabel = "Pemesan",
                    name = order.userName,
                    subtitle = "Pemesan / Pelanggan",
                    extra = order.userPhone,
                    chatLabel = "Chat Pemesan",
                    onChat = { onChat(order.id) },
                    onCall = {
                        context.dial(order.userPhone)
                    },
                )
            }

            // STATUS PROGRES
            CardSection {
                SectionTitle("Status Progres Layanan")
                Spacer(Modifier.height(12.dp))
                VerticalTimeline(buildTimeline(d))
            }

            // DETAIL LAYANAN
            CardSection {
                Text(
                    order.serviceName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    order.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedColor()
                )

                Spacer(Modifier.height(12.dp))

                IconText(
                    Icons.Filled.Event,
                    order.scheduleText
                )

                Spacer(Modifier.height(8.dp))

                IconText(
                    Icons.Filled.LocationOn,
                    order.userAddress
                )

                order.customerNote?.let { note ->
                    Spacer(Modifier.height(12.dp))

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SandTint)
                            .padding(12.dp),
                    ) {
                        Text(
                            "Catatan dari Pemesan",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            "“$note”",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // RINCIAN PEMBAYARAN / PENDAPATAN
            CardSection {
                if (role == UserRole.USER) {
                    SectionTitle("Rincian Pembayaran")
                    Spacer(Modifier.height(8.dp))

                    PriceRow("Tarif Jasa", order.servicePrice)

                    if (order.addOnPrice > 0) {
                        PriceRow("Biaya Tambahan", order.addOnPrice)
                    }

                    if (order.appFee > 0) {
                        PriceRow("Biaya Layanan Aplikasi", order.appFee)
                    }

                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp)
                    )

                    PriceRow(
                        "Total Pembayaran",
                        order.totalPrice,
                        bold = true
                    )

                } else {
                    SectionTitle("Rincian Pendapatan Mitra")
                    Spacer(Modifier.height(8.dp))

                    PriceRow(
                        "Tarif Jasa",
                        order.servicePrice + order.addOnPrice
                    )

                    PriceRow(
                        "Potongan Biaya Platform",
                        -order.platformCut,
                        valueColor = Secondary700
                    )

                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp)
                    )

                    PriceRow(
                        "Penghasilan Bersih",
                        order.providerEarning,
                        bold = true
                    )
                }
            }
        }
    }

    // DIALOG PEMBATALAN
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
            },
            title = {
                Text("Batalkan pesanan?")
            },
            text = {
                Text(
                    "Pesanan yang sudah dibatalkan tidak dapat dikembalikan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false

                        viewModel.cancelOrder {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Pesanan berhasil dibatalkan"
                                )
                            }
                        }
                    }
                ) {
                    Text(
                        "Ya, Batalkan",
                        color = Secondary700
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                    }
                ) {
                    Text("Tidak")
                }
            },
        )
    }
}

// ------------------------------------------------------------------
// HEADER STATUS PESANAN
// ------------------------------------------------------------------

@Composable
private fun StatusHeaderCard(
    order: Order,
    role: UserRole,
    hasReview: Boolean
) {
    val cancelled = order.status == OrderStatus.DIBATALKAN

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Nomor Pesanan",
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedColor()
                    )

                    Text(
                        "#${order.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                StatusBadge(order.status)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (cancelled) Secondary100 else Primary100
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = if (cancelled) Secondary700 else Primary500,
                    modifier = Modifier.size(20.dp),
                )

                Text(
                    statusMessage(
                        role = role,
                        status = order.status,
                        hasReview = hasReview
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// PESAN STATUS
// ------------------------------------------------------------------

private fun statusMessage(
    role: UserRole,
    status: OrderStatus,
    hasReview: Boolean
): String = when (role) {

    UserRole.USER -> when (status) {

        OrderStatus.MENUNGGU ->
            "Menunggu konfirmasi dari penyedia jasa."

        OrderStatus.DITERIMA ->
            "Penyedia jasa telah menerima pesananmu dan akan segera datang."

        OrderStatus.DIKERJAKAN ->
            "Teknisi sedang melakukan pengerjaan di lokasi Anda."

        OrderStatus.SELESAI -> {
            if (hasReview) {
                "Layanan ini sudah Anda beri ulasan. Terima kasih!"
            } else {
                "Layanan telah selesai dikerjakan. Jangan lupa beri ulasan."
            }
        }

        OrderStatus.DIBATALKAN ->
            "Pesanan ini telah dibatalkan."
    }

    UserRole.PROVIDER -> when (status) {

        OrderStatus.MENUNGGU ->
            "Pesanan baru masuk. Segera perbarui status pesanan."

        OrderStatus.DITERIMA ->
            "Anda telah menerima pesanan. Siapkan pengerjaan sesuai jadwal."

        OrderStatus.DIKERJAKAN ->
            "Anda sedang dalam tahap pengerjaan pesanan ini. Pastikan SOP keselamatan kerja dipatuhi."

        OrderStatus.SELESAI ->
            "Pesanan telah selesai. Penghasilan masuk ke dompet mitra."

        OrderStatus.DIBATALKAN ->
            "Pesanan ini telah dibatalkan oleh pemesan."
    }
}

// ------------------------------------------------------------------
// CARD SECTION
// ------------------------------------------------------------------

@Composable
private fun CardSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            content = content
        )
    }
}

// ------------------------------------------------------------------
// PRICE ROW
// ------------------------------------------------------------------

@Composable
private fun PriceRow(
    label: String,
    amount: Long,
    bold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (bold)
                MaterialTheme.typography.labelLarge
            else
                MaterialTheme.typography.bodyMedium,
            color = if (bold) Neutral900 else mutedColor(),
        )

        val text = if (amount < 0) {
            "-" + (-amount).toRupiah()
        } else {
            amount.toRupiah()
        }

        Text(
            text,
            style = if (bold)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyMedium,
            color = if (valueColor != Color.Unspecified) {
                valueColor
            } else if (bold) {
                Primary500
            } else {
                Neutral900
            },
        )
    }
}

// ------------------------------------------------------------------
// DETAIL ACTION BAR
// ------------------------------------------------------------------

@Composable
private fun DetailActionBar(
    role: UserRole,
    status: OrderStatus,
    hasReview: Boolean,
    onCancel: () -> Unit,
    onReview: () -> Unit,
    onReorder: () -> Unit,
    onHelp: () -> Unit,
    onUpdateStatus: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(12.dp)
    val buttonModifier = Modifier.heightIn(min = 48.dp)

    // Penyedia hanya punya aksi selama pesanan masih aktif.
    if (role == UserRole.PROVIDER && !status.isActive) return

    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            when {

                role == UserRole.PROVIDER -> {
                    Button(
                        onClick = onUpdateStatus,
                        shape = buttonShape,
                        modifier = buttonModifier.weight(1f)
                    ) {
                        Text("Update Status")

                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                status == OrderStatus.SELESAI -> {

                    OutlinedButton(
                        onClick = onReorder,
                        shape = buttonShape,
                        modifier = buttonModifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Pesan Lagi")
                    }

                    Button(
                        onClick = onReview,
                        shape = buttonShape,
                        modifier = buttonModifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent500,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (hasReview) "Lihat Ulasan"
                            else "Beri Ulasan"
                        )
                    }
                }

                status.canBeCancelled -> {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = buttonShape,
                        border = BorderStroke(1.dp, Secondary700),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Secondary700
                        ),
                        modifier = buttonModifier.weight(1f),
                    ) {
                        Text("Batalkan Pesanan")
                    }
                }

                status == OrderStatus.DIKERJAKAN -> {

                    OutlinedButton(
                        onClick = onHelp,
                        shape = buttonShape,
                        modifier = buttonModifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.SupportAgent,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text("Bantuan")
                    }

                    Button(
                        onClick = {},
                        enabled = false,
                        shape = buttonShape,
                        modifier = buttonModifier.weight(1f)
                    ) {
                        Text("Sedang Dikerjakan")
                    }
                }

                else -> {
                    Button(
                        onClick = onReorder,
                        shape = buttonShape,
                        modifier = buttonModifier.weight(1f)
                    ) {
                        Text("Pesan Lagi")
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// DIAL PHONE
// ------------------------------------------------------------------

private fun Context.dial(phone: String) {
    val number = phone.filter {
        it.isDigit() || it == '+'
    }

    startActivity(
        Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:$number")
        )
    )
}
