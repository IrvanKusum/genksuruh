@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.genksuruh.feature.promo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Modifier.dashedBorder(
    color: Color,
    radius: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp
): Modifier = this.drawBehind {
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
            .padding(horizontal = 8.dp, vertical = 2.dp)
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
                if (selected) PromoColors.Primary else Color(0xFFD5E1E8),
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else PromoColors.TextDark
        )
    }
}

@Composable
fun SimpleTabs(titles: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        titles.forEachIndexed { index, title ->
            val isSelected = index == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PromoColors.Primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else PromoColors.TextMuted
                )
            }
        }
    }
}

@Composable
fun QuotaBar(claimed: Int, quota: Int) {
    LinearProgressIndicator(
        progress = { if (quota == 0) 0f else (claimed.toFloat() / quota).coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(50)),
        color = PromoColors.Primary,
        trackColor = PromoColors.SoftBlue
    )
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
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = PromoColors.TextDark
    )
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
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$value",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PromoColors.Primary
            )
            Text(text = label, fontSize = 11.sp, color = PromoColors.TextMuted)
        }
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    buttonLabel: String?,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 64.sp)
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = PromoColors.TextDark
        )
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = PromoColors.TextMuted,
            textAlign = TextAlign.Center
        )
        if (buttonLabel != null) {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
fun PromoCard(promo: Promo, onClick: () -> Unit, trailing: @Composable () -> Unit) {
    val dimmed = promo.isFull || promo.isExpired
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.6f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(PromoColors.SoftBlue, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = promo.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = promo.badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
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
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = promo.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PromoColors.TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = promo.providerName,
                    fontSize = 11.sp,
                    color = PromoColors.TextMuted
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CodeChip(promo.code)
                    Text(
                        text = "s/d ${formatTanggal(promo.endDate)}",
                        fontSize = 10.sp,
                        color = PromoColors.TextMuted
                    )
                }
                QuotaBar(promo.claimed, promo.quota)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${promo.claimed}/${promo.quota} terklaim",
                        fontSize = 10.sp,
                        color = PromoColors.TextMuted
                    )
                    trailing()
                }
            }
        }
    }
}

@Composable
fun VoucherTicketCard(
    promo: Promo,
    status: VoucherStatus,
    usedAt: String?,
    onUse: () -> Unit
) {
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
                        .width(92.dp)
                        .fillMaxHeight()
                        .background(if (active) PromoColors.Primary else PromoColors.Terpakai),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = promo.badge,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = promo.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PromoColors.TextDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        StatusBadge(status)
                    }
                    Text(
                        text = "${promo.serviceName} • ${promo.providerName}",
                        fontSize = 11.sp,
                        color = PromoColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CodeChip(promo.code)
                    Text(
                        text = when (status) {
                            VoucherStatus.AKTIF -> "Berlaku s/d ${formatTanggal(promo.endDate)}"
                            VoucherStatus.TERPAKAI -> "Dipakai ${formatTanggal(usedAt ?: promo.endDate)}"
                            VoucherStatus.KEDALUWARSA -> "Berakhir ${formatTanggal(promo.endDate)}"
                        },
                        fontSize = 11.sp,
                        color = PromoColors.TextMuted
                    )
                    if (active) SmallButton("Gunakan", onClick = onUse)
                }
            }
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 84.dp, y = (-8).dp)
                    .size(16.dp)
                    .background(PromoColors.Background, CircleShape)
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 84.dp, y = 8.dp)
                    .size(16.dp)
                    .background(PromoColors.Background, CircleShape)
            )
        }
    }
}

@Composable
fun ClaimRow(item: ClaimItem, onMark: () -> Unit) {
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
                    text = item.claim.userName.take(1),
                    color = PromoColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.claim.userName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PromoColors.TextDark
                )
                Text(text = item.promoCode, fontSize = 11.sp, color = PromoColors.TextMuted)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(item.status)
                if (item.status == VoucherStatus.AKTIF) {
                    OutlinedButton(
                        onClick = onMark,
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Tandai Terpakai", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OverflowMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = PromoColors.TextMuted
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Hapus", color = PromoColors.Kedaluwarsa) },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

// ===================== DIALOG =====================

@Composable
fun ConfirmDeleteDialog(promoTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = PromoColors.Kedaluwarsa) },
        title = { Text("Hapus promo ini?", fontWeight = FontWeight.SemiBold) },
        text = {
            Text("Promo \"$promoTitle\" akan dihapus dan voucher yang sudah diklaim tidak bisa dipakai lagi.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Kedaluwarsa)
            ) {
                Text("Ya, Hapus")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun MarkUsedDialog(userName: String, code: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Check, contentDescription = null, tint = PromoColors.Primary) },
        title = { Text("Tandai voucher terpakai?", fontWeight = FontWeight.SemiBold) },
        text = {
            Text("Voucher $code milik $userName akan ditandai sebagai Terpakai dan tidak bisa dipakai lagi.")
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Ya, Tandai") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ClaimSuccessDialog(code: String, onSeeVouchers: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = PromoColors.Aktif) },
        title = { Text("Voucher berhasil diklaim!", fontWeight = FontWeight.SemiBold) },
        text = { Text("$code sudah masuk ke Voucher Saya.") },
        confirmButton = {
            Button(
                onClick = onSeeVouchers,
                colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
            ) {
                Text("Lihat Voucher Saya")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun VoucherSheet(promo: Promo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Gunakan Voucher", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(PromoColors.SoftBlue, RoundedCornerShape(12.dp))
                    .dashedBorder(PromoColors.Primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
            FakeQr(promo.code)
            Text(
                text = "Tunjukkan kode ini saat checkout",
                fontSize = 13.sp,
                color = PromoColors.TextMuted
            )
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Tutup") }
        }
    }
}

/** QR dummy (hiasan). */
@Composable
fun FakeQr(code: String, qrSize: Dp = 140.dp) {
    val n = 21
    val cells = remember(code) {
        val random = java.util.Random(code.hashCode().toLong())
        List(n * n) { random.nextInt(100) < 48 }
    }
    Canvas(
        Modifier
            .size(qrSize)
            .background(Color.White)
            .border(1.dp, Color(0xFFD5E1E8))
    ) {
        val cell = this.size.width / n
        for (y in 0 until n) {
            for (x in 0 until n) {
                val finder = (x < 7 && y < 7) || (x >= n - 7 && y < 7) || (x < 7 && y >= n - 7)
                val on = if (finder) {
                    val fx = if (x >= n - 7) x - (n - 7) else x
                    val fy = if (y >= n - 7) y - (n - 7) else y
                    fx == 0 || fx == 6 || fy == 0 || fy == 6 || (fx in 2..4 && fy in 2..4)
                } else {
                    cells[y * n + x]
                }
                if (on) {
                    drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell, cell))
                }
            }
        }
    }
}