package com.example.genksuruh.feature.review

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.genksuruh.core.theme.*
import java.util.Locale

fun formatAvg(v: Double): String = String.format(Locale("id", "ID"), "%.1f", v)

// ---------- Bintang ----------
@Composable
fun StarRow(rating: Int, size: Dp = 14.dp) {
    Row {
        for (i in 1..5) {
            Icon(
                Icons.Default.Star, contentDescription = null,
                tint = if (i <= rating) SandYellow else StarEmpty,
                modifier = Modifier.size(size),
            )
        }
    }
}

// ---------- Avatar inisial ----------
@Composable
fun Avatar(name: String, size: Dp = 40.dp) {
    val palette = listOf(BlueSoft, Color(0xFFFFE5DD), Color(0xFFFFF1CC), Color(0xFFE0F5EC))
    Box(
        Modifier.size(size).clip(CircleShape).background(palette[name.length % palette.size]),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.first().uppercase(), fontWeight = FontWeight.SemiBold, color = OceanDark)
    }
}

// ---------- Skeleton ----------
@Composable
fun SkeletonBox(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val a by t.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha",
    )
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(StarEmpty.copy(alpha = a)))
}

// ---------- Kartu ringkasan rating ----------
@Composable
fun RatingSummaryCard(average: Double, total: Int, distribution: Map<Int, Int>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(104.dp)) {
                Text(formatAvg(average), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = OceanDark)
                StarRow(average.toInt(), 16.dp)
                Spacer(Modifier.height(4.dp))
                Text("$total ulasan", style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (star in 5 downTo 1) {
                    val count = distribution[star] ?: 0
                    val fraction = if (total == 0) 0f else count.toFloat() / total
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$star", style = MaterialTheme.typography.labelSmall, color = TextGray)
                        Icon(Icons.Default.Star, null, tint = SandYellow, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        // Bar distribusi dibuat manual (track + isi)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(BlueSoft)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .background(OceanBlue)
                            )
                        }
                        Text(
                            "$count",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray,
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}

// ---------- Chip filter bintang ----------
@Composable
fun StarFilterChips(
    distribution: Map<Int, Int>,
    total: Int,
    selected: Int?,
    onSelect: (Int?) -> Unit,
) {
    val options: List<Pair<Int?, String>> =
        listOf<Pair<Int?, String>>(null to "Semua ($total)") +
                (5 downTo 1).map { Pair<Int?, String>(it, "$it ★ (${distribution[it] ?: 0})") }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { (star, label) ->
            val isSel = star == selected
            Surface(
                onClick = { onSelect(star) },
                shape = CircleShape,
                color = if (isSel) OceanBlue else Color.White,
                border = if (isSel) null else BorderStroke(1.dp, BorderGray),
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) Color.White else TextDark,
                )
            }
        }
    }
}

// ---------- Kartu ulasan ----------
@Composable
fun ReviewCard(review: Review, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var overflow by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            if (review.isOwn) {
                Box(Modifier.width(4.dp).fillMaxHeight().background(OceanBlue))
            }
            Column(Modifier.weight(1f).padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(review.name)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(review.name, style = MaterialTheme.typography.titleSmall)
                            if (review.isOwn) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Ulasan kamu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OceanBlue,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BlueSoft)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(review.date, style = MaterialTheme.typography.bodySmall, color = TextGray)
                    }
                    // Titik tiga HANYA untuk ulasan milik sendiri
                    if (review.isOwn) {
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Default.MoreVert, "Menu ulasan", tint = TextGray)
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                                shape = RoundedCornerShape(12.dp),
                                containerColor = Color.White,
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Ulasan", color = TextDark) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = TextDark) },
                                    onClick = { menuOpen = false; onEdit() },
                                )
                                DropdownMenuItem(
                                    text = { Text("Hapus Ulasan", color = DangerRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = DangerRed) },
                                    onClick = { menuOpen = false; onDelete() },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                StarRow(review.rating)
                Spacer(Modifier.height(6.dp))
                Text(
                    review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { if (!expanded) overflow = it.hasVisualOverflow },
                )
                if (overflow || expanded) {
                    Text(
                        if (expanded) "Tutup" else "Selengkapnya",
                        style = MaterialTheme.typography.labelMedium,
                        color = OceanBlue,
                        modifier = Modifier.padding(top = 4.dp).clickable { expanded = !expanded },
                    )
                }
            }
        }
    }
}

// ---------- Dialog hapus ----------
@Composable
fun DeleteReviewDialog(
    serviceName: String,
    state: DeleteState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val loading = state == DeleteState.Loading
    Dialog(onDismissRequest = { if (!loading) onCancel() }) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.width(312.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(DangerSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Delete, null, tint = DangerRed, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Hapus ulasan ini?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ulasan kamu untuk \"$serviceName\" akan dihapus dan rating jasa akan dihitung ulang. Tindakan ini tidak bisa dibatalkan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                )
                if (state == DeleteState.Error) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Gagal menghapus ulasan. Periksa koneksi internet kamu.",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerRed,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !loading,
                        modifier = Modifier.weight(1f).height(44.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) { Text("Batal", color = TextDark, fontWeight = FontWeight.Medium, fontSize = 13.sp) }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            disabledContainerColor = DangerRed.copy(alpha = 0.6f),
                            disabledContentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Menghapus...", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        } else {
                            Text(
                                if (state == DeleteState.Error) "Coba Lagi" else "Ya, Hapus",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}