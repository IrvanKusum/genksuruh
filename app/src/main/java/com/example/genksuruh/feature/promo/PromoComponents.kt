package com.example.genksuruh.feature.promo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

// ======================= Dasar =======================

fun Modifier.dashedBorder(color: Color, radius: Dp = 12.dp, strokeWidth: Dp = 1.5.dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(radius.toPx()),
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
            )
        )
    }

@Composable
fun StatusBadge(status: VoucherStatus) {
    val color = when (status) {
        VoucherStatus.AKTIF -> PromoColors.Aktif
        VoucherStatus.TERPAKAI -> PromoColors.Terpakai
        VoucherStatus.KEDALUWARSA -> PromoColors.Kedaluwarsa
    }
    Text(
        text = status.label,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun CodeChip(code: String) {
    Text(
        text = code,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = PromoColors.Primary,
        modifier = Modifier
            .dashedBorder(PromoColors.Primary, 6.dp, 1.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun ChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) PromoColors.Primary else Color.White)
            .border(
                1.dp,
                if (selected) PromoColors.Primary else PromoColors.Outline,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else PromoColors.TextDark
        )
    }
}

/** Tab dengan garis bawah (Promo Tersedia | Voucher Saya). */
@Composable
fun UnderlineTabs(titles: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        titles.forEachIndexed { i, title ->
            val sel = i == selected
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (sel) PromoColors.Primary else PromoColors.TextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(if (sel) 3.dp else 1.dp)
                        .background(if (sel) PromoColors.Primary else PromoColors.Outline)
                )
            }
        }
    }
}

/** Tombol segmen dua pilihan (Persen | Nominal). */
@Composable
fun SegmentedToggle(titles: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, PromoColors.Outline, RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        titles.forEachIndexed { i, title ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .background(if (sel) PromoColors.Primary else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (sel) Color.White else PromoColors.TextMuted
                )
            }
        }
    }
}

@Composable
fun QuotaBar(claimed: Int, quota: Int) {
    val fraction = if (quota == 0) 0f else (claimed.toFloat() / quota).coerceIn(0f, 1f)
    Box(
        Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(PromoColors.SoftBlue)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(PromoColors.Primary)
        )
    }
}

@Composable
fun SmallButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PromoColors.Coral,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFD5DDE3),
            disabledContentColor = PromoColors.TextMuted
        )
    ) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PromoColors.TextDark)
}

@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PromoColors.Primary)
            Text(label, fontSize = 11.sp, color = PromoColors.TextMuted, maxLines = 1)
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonLabel: String?,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF7FBCDD), modifier = Modifier.size(96.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PromoColors.TextDark)
        Text(subtitle, fontSize = 13.sp, color = PromoColors.TextMuted, textAlign = TextAlign.Center)
        if (buttonLabel != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
            ) { Text(buttonLabel, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ======================= Header =======================

@Composable
fun PromoHeader(title: String, isProvider: Boolean, onToggleProvider: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PromoColors.TextDark)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Notifications, contentDescription = "Notifikasi",
                    tint = PromoColors.TextDark
                )
            }
            // Tombol bulat: ketuk untuk pindah mode User <-> Penyedia.
            // Saat mode Penyedia aktif, tombol punya cincin kuning.
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PromoColors.Primary)
                    .then(
                        if (isProvider) Modifier.border(2.dp, PromoColors.Sand, CircleShape)
                        else Modifier
                    )
                    .clickable { onToggleProvider(!isProvider) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = if (isProvider) "Beralih ke mode pengguna"
                    else "Beralih ke mode penyedia",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ======================= Kartu =======================

@Composable
private fun PromoThumbnail(promo: Promo) {
    Box(
        Modifier
            .size(72.dp)
            .background(PromoColors.SoftBlue, RoundedCornerShape(12.dp))
    ) {
        Text(promo.emoji, fontSize = 32.sp, modifier = Modifier.align(Alignment.Center))
        Text(
            promo.badge, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            color = PromoColors.TextDark,
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(
                    PromoColors.Sand,
                    RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoCard(promo: Promo, onClick: () -> Unit, trailing: @Composable () -> Unit) {
    val dim = promo.isFull || promo.isExpired
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dim) 0.6f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PromoThumbnail(promo)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    promo.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = PromoColors.TextDark, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(promo.providerName, fontSize = 11.sp, color = PromoColors.TextMuted)
                CodeChip(promo.code)
                Text(
                    "Berlaku s/d ${formatTanggal(promo.endDate)}",
                    fontSize = 10.sp, color = PromoColors.TextMuted
                )
                QuotaBar(promo.claimed, promo.quota)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${promo.claimed}/${promo.quota} terklaim",
                        fontSize = 10.sp, color = PromoColors.TextMuted
                    )
                    trailing()
                }
            }
        }
    }
}

/** Kartu di "Promo Saya" (penyedia): titik tiga di kanan atas, badge status di bawah. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPromoCard(promo: Promo, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PromoThumbnail(promo)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        promo.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = PromoColors.TextDark, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    OverflowMenu(onEdit = onEdit, onDelete = onDelete)
                }
                CodeChip(promo.code)
                QuotaBar(promo.claimed, promo.quota)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${promo.claimed}/${promo.quota} terklaim",
                        fontSize = 10.sp, color = PromoColors.TextMuted
                    )
                    StatusBadge(if (promo.isExpired) VoucherStatus.KEDALUWARSA else VoucherStatus.AKTIF)
                }
            }
        }
    }
}

/** Voucher bergaya tiket: sisi kiri nilai diskon, garis putus-putus, lekukan setengah lingkaran. */
@Composable
fun VoucherTicketCard(promo: Promo, status: VoucherStatus, usedAt: String?, onUse: () -> Unit) {
    val active = status == VoucherStatus.AKTIF
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (active) 1f else 0.65f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .width(88.dp)
                        .fillMaxHeight()
                        .background(if (active) PromoColors.Primary else PromoColors.Terpakai),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        promo.badge, color = Color.White, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                }
                Canvas(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                ) {
                    drawLine(
                        color = Color(0xFFBFD5E0),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            promo.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = PromoColors.TextDark, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        StatusBadge(status)
                    }
                    CodeChip(promo.code)
                    Text(
                        when (status) {
                            VoucherStatus.AKTIF -> "Berlaku s/d ${formatTanggal(promo.endDate)}"
                            VoucherStatus.TERPAKAI -> "Dipakai ${formatTanggal(usedAt ?: promo.endDate)}"
                            VoucherStatus.KEDALUWARSA -> "Berakhir ${formatTanggal(promo.endDate)}"
                        },
                        fontSize = 10.sp, color = PromoColors.TextMuted
                    )
                    if (active) {
                        Spacer(Modifier.height(2.dp))
                        SmallButton("Gunakan", onClick = onUse)
                    }
                }
            }
            // lekukan tiket (setengah lingkaran di garis pemisah)
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 80.dp, y = (-8).dp)
                    .size(16.dp)
                    .background(PromoColors.Background, CircleShape)
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 80.dp, y = 8.dp)
                    .size(16.dp)
                    .background(PromoColors.Background, CircleShape)
            )
        }
    }
}

@Composable
fun ClaimRow(claim: VoucherClaim, status: VoucherStatus, onMark: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(PromoColors.SoftBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    claim.userName.take(1), color = PromoColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    claim.userName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = PromoColors.TextDark
                )
                Spacer(Modifier.height(4.dp))
                StatusBadge(status)
            }
            if (status == VoucherStatus.AKTIF) {
                OutlinedButton(
                    onClick = onMark,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) { Text("Tandai Terpakai", fontSize = 10.sp) }
            }
        }
    }
}

@Composable
fun OverflowMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Menu", tint = PromoColors.TextMuted)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Edit") }, onClick = { expanded = false; onEdit() })
            DropdownMenuItem(
                text = { Text("Hapus", color = PromoColors.Kedaluwarsa) },
                onClick = { expanded = false; onDelete() }
            )
        }
    }
}

// ======================= Dialog =======================

@Composable
private fun PromoDialog(
    onDismiss: () -> Unit,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    text: String,
    actions: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0.5f) }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.width(312.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                }
                Text(
                    title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    color = PromoColors.TextDark, textAlign = TextAlign.Center
                )
                Text(
                    text, fontSize = 13.sp, color = PromoColors.TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                actions()
            }
        }
    }
}

@Composable
private fun DialogButtons(
    cancelLabel: String,
    confirmLabel: String,
    confirmColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PromoColors.Outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PromoColors.TextMuted)
        ) { Text(cancelLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
        ) { Text(confirmLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun ConfirmDeleteDialog(promo: Promo, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    PromoDialog(
        onDismiss = onDismiss,
        icon = Icons.Outlined.Delete,
        iconTint = PromoColors.Kedaluwarsa,
        iconBg = Color(0xFFFDECEC),
        title = "Hapus promo ini?",
        text = "Promo \"${promo.title}\" akan dihapus dan voucher yang sudah diklaim tidak bisa dipakai lagi."
    ) {
        DialogButtons("Batal", "Ya, Hapus", PromoColors.Kedaluwarsa, onDismiss, onConfirm)
    }
}

@Composable
fun MarkUsedDialog(claim: VoucherClaim, code: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    PromoDialog(
        onDismiss = onDismiss,
        icon = Icons.Outlined.Check,
        iconTint = PromoColors.Primary,
        iconBg = PromoColors.SoftBlue,
        title = "Tandai voucher terpakai?",
        text = "Voucher $code milik ${claim.userName} akan ditandai sebagai Terpakai dan tidak bisa dipakai lagi."
    ) {
        DialogButtons("Batal", "Ya, Tandai", PromoColors.Primary, onDismiss, onConfirm)
    }
}

@Composable
fun ClaimSuccessDialog(code: String, onSeeVouchers: () -> Unit, onDismiss: () -> Unit) {
    PromoDialog(
        onDismiss = onDismiss,
        icon = Icons.Outlined.ConfirmationNumber,
        iconTint = PromoColors.Aktif,
        iconBg = Color(0xFFDDF1EA),
        title = "Voucher berhasil diklaim!",
        text = "$code sudah masuk ke Voucher Saya."
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onSeeVouchers,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
            ) { Text("Lihat Voucher Saya", fontWeight = FontWeight.SemiBold) }
            TextButton(onClick = onDismiss) { Text("Tutup", color = PromoColors.TextMuted) }
        }
    }
}

@Composable
fun ClaimFailedDialog(message: String, onDismiss: () -> Unit) {
    PromoDialog(
        onDismiss = onDismiss,
        icon = Icons.Outlined.Warning,
        iconTint = Color(0xFFE08A1E),
        iconBg = Color(0xFFFFF1DC),
        title = "Voucher tidak bisa diklaim",
        text = message
    ) {
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Primary)
        ) { Text("Mengerti", fontWeight = FontWeight.SemiBold) }
    }
}

// ======================= Bottom sheet Gunakan Voucher =======================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherSheet(promo: Promo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Gunakan Voucher", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
            FakeQr(promo.code)
            Text("Tunjukkan kode ini saat checkout", fontSize = 13.sp, color = PromoColors.TextMuted)
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Tutup", fontWeight = FontWeight.SemiBold) }
        }
    }
}

/** QR dummy (hiasan). Nanti ganti library QR asli (mis. ZXing). */
@Composable
fun FakeQr(code: String, qrSize: Dp = 140.dp) {
    val n = 21
    val cells = remember(code) {
        val r = java.util.Random(code.hashCode().toLong())
        List(n * n) { r.nextInt(100) < 48 }
    }
    Canvas(
        Modifier
            .size(qrSize)
            .background(Color.White)
            .border(1.dp, PromoColors.Outline)
    ) {
        val cell = this.size.width / n
        for (y in 0 until n) for (x in 0 until n) {
            val finder = (x < 7 && y < 7) || (x >= n - 7 && y < 7) || (x < 7 && y >= n - 7)
            val on = if (finder) {
                val fx = if (x >= n - 7) x - (n - 7) else x
                val fy = if (y >= n - 7) y - (n - 7) else y
                fx == 0 || fx == 6 || fy == 0 || fy == 6 || (fx in 2..4 && fy in 2..4)
            } else cells[y * n + x]
            if (on) drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell, cell))
        }
    }
}