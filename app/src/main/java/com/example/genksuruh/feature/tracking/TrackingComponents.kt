package com.example.genksuruh.feature.tracking

import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.genksuruh.ui.theme.*

// ---- Turunan dari palet tim (tidak membuat warna baru) ----
internal val SandTint = Accent500.copy(alpha = 0.25f)
internal val DisabledColor = Neutral900.copy(alpha = 0.25f)

@Composable
internal fun mutedColor(): Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

// ------------------------------------------------------------------ top bar & umum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        },
        actions = actions,
        // Samakan jarak status bar dengan header pada halaman Ulasan.
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
    )
}

@Composable
fun AvatarButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(end = 12.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(Primary500)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Person, contentDescription = "Ganti peran (demo)", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

/** Badge status: selalu ada teks + ikon, jadi makna tidak bergantung warna saja. */
@Composable
fun StatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when (status) {
        OrderStatus.MENUNGGU -> Triple(SandTint, Neutral900, Icons.Filled.HourglassTop)
        OrderStatus.DITERIMA -> Triple(Primary100, Primary700, Icons.Filled.CheckCircle)
        OrderStatus.DIKERJAKAN -> Triple(Primary500, Color.White, Icons.Filled.Build)
        OrderStatus.SELESAI -> Triple(Primary500, Color.White, Icons.Filled.Check)
        OrderStatus.DIBATALKAN -> Triple(Secondary700, Color.White, Icons.Filled.Close)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Text(status.label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
fun CategoryIcon(category: ServiceCategory, modifier: Modifier = Modifier) {
    val (icon, bg) = when (category) {
        ServiceCategory.SERVIS_IT -> Icons.Filled.Laptop to SandTint
        ServiceCategory.EDUKASI -> Icons.Filled.School to Primary100
        ServiceCategory.KREATIF -> Icons.Filled.Palette to Primary100
        ServiceCategory.HOME_SERVICE -> Icons.Filled.Air to Primary100

        ServiceCategory.ANTAR_MAKANAN -> Icons.Filled.Restaurant to Color(0xFFFFE8CC)
    }
    Box(
        modifier = modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Primary500)
    }
}

@Composable
fun IconText(icon: ImageVector, text: String, modifier: Modifier = Modifier, maxLines: Int = 2) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = Primary500, modifier = Modifier.size(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(Primary100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Receipt, contentDescription = null, tint = Primary500, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = mutedColor(), textAlign = TextAlign.Center)
        if (actionLabel != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(actionLabel)
            }
        }
    }
}

// ------------------------------------------------------------------ kartu pesanan

@Composable
fun OrderCard(
    order: Order,
    role: UserRole,
    onDetailClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // User melihat penyedia; Penyedia melihat pemesan.
    val counterpart =
        if (role == UserRole.USER) "${order.providerCompany} (${order.providerName})" else order.userName

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${order.id} • ${order.categoryLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusBadge(order.status)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryIcon(order.category)
                Text(order.serviceName, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary100.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconText(Icons.Filled.Person, counterpart, maxLines = 1)
                IconText(Icons.Filled.Schedule, order.scheduleText, maxLines = 1)
                val progress = order.progressPercent
                if (order.status == OrderStatus.DIKERJAKAN && progress != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progres pengerjaan", style = MaterialTheme.typography.bodySmall)
                        Text("$progress%", style = MaterialTheme.typography.labelMedium, color = Primary500)
                    }
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        color = Primary500,
                        trackColor = Color.White,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Total Biaya", style = MaterialTheme.typography.bodySmall, color = mutedColor())
                    Text(order.totalPrice.toRupiah(), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                }
                FilledTonalIconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Chat")
                }
                Button(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent500,
                        contentColor = Color.White
                    )
                ) {
                    Text("Lihat Detail")
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/** Kerangka kartu saat loading (shimmer sederhana lewat animasi alpha). */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val c = Primary100.copy(alpha = alpha)

    @Composable
    fun Bar(width: Int, height: Int = 14) {
        Box(Modifier.width(width.dp).height(height.dp).clip(RoundedCornerShape(50)).background(c))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Bar(120, 12)
                Bar(80, 24)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(c))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Bar(160)
                    Bar(110, 12)
                }
            }
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(c))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Bar(100, 20)
                Bar(140, 40)
            }
        }
    }
}

// ------------------------------------------------------------------ timeline

enum class StepState { DONE, CURRENT, UPCOMING, CANCELLED }

data class TimelineItem(
    val title: String,
    val time: String,
    val description: String,
    val state: StepState,
)

private val flowSteps = listOf(OrderStatus.MENUNGGU, OrderStatus.DITERIMA, OrderStatus.DIKERJAKAN, OrderStatus.SELESAI)

private fun titleOf(s: OrderStatus) = when (s) {
    OrderStatus.MENUNGGU -> "Menunggu Konfirmasi"
    OrderStatus.DITERIMA -> "Diterima Mitra"
    OrderStatus.DIKERJAKAN -> "Sedang Dikerjakan"
    OrderStatus.SELESAI -> "Layanan Selesai"
    OrderStatus.DIBATALKAN -> "Pesanan Dibatalkan"
}

private fun defaultDescOf(s: OrderStatus) = when (s) {
    OrderStatus.MENUNGGU -> "Menunggu konfirmasi penyedia jasa"
    OrderStatus.DITERIMA -> "Penyedia jasa menyetujui jadwal"
    OrderStatus.DIKERJAKAN -> "Pengerjaan sedang berjalan"
    OrderStatus.SELESAI -> "Konfirmasi hasil pengerjaan"
    OrderStatus.DIBATALKAN -> "Pesanan dibatalkan"
}

/** Menyusun timeline dari status_history: Menunggu -> Diterima -> Dikerjakan -> Selesai. */
fun buildTimeline(detail: OrderDetail): List<TimelineItem> {
    val status = detail.order.status
    val history = detail.history

    if (status == OrderStatus.DIBATALKAN) {
        val reached = history.filter { it.status != OrderStatus.DIBATALKAN }.map {
            TimelineItem(titleOf(it.status), it.timeText, it.note ?: defaultDescOf(it.status), StepState.DONE)
        }
        val cancelled = history.lastOrNull { it.status == OrderStatus.DIBATALKAN }
        return reached + TimelineItem(
            titleOf(OrderStatus.DIBATALKAN),
            cancelled?.timeText ?: "",
            cancelled?.note ?: defaultDescOf(OrderStatus.DIBATALKAN),
            StepState.CANCELLED,
        )
    }

    val currentIndex = flowSteps.indexOf(status)
    return flowSteps.mapIndexed { index, s ->
        val entry = history.lastOrNull { it.status == s }
        val state = when {
            status == OrderStatus.SELESAI -> StepState.DONE
            index < currentIndex -> StepState.DONE
            index == currentIndex -> StepState.CURRENT
            else -> StepState.UPCOMING
        }
        TimelineItem(
            title = titleOf(s),
            time = entry?.timeText ?: if (state == StepState.UPCOMING) "Menunggu" else "",
            description = entry?.note ?: defaultDescOf(s),
            state = state,
        )
    }
}

@Composable
fun VerticalTimeline(items: List<TimelineItem>, modifier: Modifier = Modifier) {
    Column(modifier) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            val nextUpcoming = items.getOrNull(index + 1)?.state == StepState.UPCOMING
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.width(24.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    StepNode(item.state)
                    if (!isLast) {
                        Connector(dashed = nextUpcoming, modifier = Modifier.weight(1f).width(2.dp))
                    }
                }
                Column(
                    Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val titleColor = when (item.state) {
                            StepState.CURRENT -> Primary500
                            StepState.CANCELLED -> Secondary700
                            StepState.UPCOMING -> mutedColor()
                            StepState.DONE -> MaterialTheme.colorScheme.onSurface
                        }
                        Text(item.title, style = MaterialTheme.typography.labelLarge, color = titleColor, modifier = Modifier.weight(1f))
                        Text(item.time, style = MaterialTheme.typography.bodySmall, color = mutedColor())
                    }
                    if (item.state == StepState.CURRENT) {
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Primary100)
                                .padding(12.dp),
                        )
                    } else {
                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = mutedColor())
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNode(state: StepState) {
    when (state) {
        StepState.DONE -> NodeCircle(Primary500) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
        StepState.CANCELLED -> NodeCircle(Secondary700) { Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
        StepState.CURRENT -> Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(Primary100),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(Primary500), contentAlignment = Alignment.Center) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
            }
        }
        StepState.UPCOMING -> Box(Modifier.size(24.dp).clip(CircleShape).border(2.dp, DisabledColor, CircleShape))
    }
}

@Composable
private fun NodeCircle(color: Color, content: @Composable () -> Unit) {
    Box(Modifier.size(24.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun Connector(dashed: Boolean, modifier: Modifier = Modifier) {
    val color = if (dashed) DisabledColor else Primary500
    Canvas(modifier) {
        drawLine(
            color = color,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2.dp.toPx(),
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null,
        )
    }
}

// ------------------------------------------------------------------ info orang

@Composable
fun PersonInfoCard(
    roleLabel: String,
    name: String,
    subtitle: String,
    extra: String,
    chatLabel: String,
    onChat: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(roleLabel, style = MaterialTheme.typography.labelMedium, color = mutedColor())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(SandTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initialsOf(name), style = MaterialTheme.typography.titleMedium)
                }
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = mutedColor())
                    Text(extra, style = MaterialTheme.typography.bodySmall, color = mutedColor())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onChat,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Filled.ChatBubbleOutline, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(chatLabel)
                }
                FilledTonalButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = "Telepon", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun initialsOf(name: String): String =
    name.split(" ", ",", ".").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }