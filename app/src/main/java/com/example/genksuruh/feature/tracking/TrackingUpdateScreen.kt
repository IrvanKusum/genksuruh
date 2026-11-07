package com.example.genksuruh.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.ui.theme.*

/** Route: tracking_update/{orderId}  (khusus Penyedia) */
@Composable
fun TrackingUpdateScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TrackingUpdateViewModel = viewModel(),
) {
    val detail by viewModel.detail.collectAsState()
    val order = detail?.order

    Scaffold(
        topBar = { TrackingTopBar("Update Status", onBack = onBack) },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrderSummaryCard(order)

            SectionTitle("Perbarui Status Pengerjaan")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Text("Status Baru", style = MaterialTheme.typography.labelLarge)
                    Text(" *", color = Secondary700, style = MaterialTheme.typography.labelLarge)
                }
                StatusDropdown(
                    current = order.status,
                    selected = viewModel.selectedStatus,
                    onSelected = viewModel::onStatusSelected,
                )
                Text(
                    "Pilih status terbaru untuk mengabarkan progres ke pemesan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedColor(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Catatan Progres", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = viewModel.note,
                    onValueChange = viewModel::onNoteChange,
                    placeholder = { Text("Tulis catatan perkembangan pekerjaan...") },
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            "${viewModel.note.length}/${TrackingUpdateViewModel.MAX_NOTE} karakter",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                )
            }

            if (viewModel.selectedStatus == OrderStatus.SELESAI) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary100)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Lightbulb, null, tint = Neutral900, modifier = Modifier.size(20.dp))
                    Text(
                        "Status Selesai akan mengirim notifikasi ke pelanggan untuk konfirmasi akhir dan pemberian ulasan.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = viewModel.canSave,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan")
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Batal", color = Neutral900)
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ringkasan Pesanan", style = MaterialTheme.typography.labelMedium, color = mutedColor())
                StatusBadge(order.status)
            }
            Text(order.serviceName, style = MaterialTheme.typography.titleMedium)
            Text("#${order.id}", style = MaterialTheme.typography.bodySmall, color = mutedColor())
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary100.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Person, null, tint = Primary500)
                Column {
                    Text("Pemesan", style = MaterialTheme.typography.bodySmall, color = mutedColor())
                    Text(order.userName, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** Dropdown status. Hanya status di depan status saat ini yang bisa dipilih. */
@Composable
private fun StatusDropdown(
    current: OrderStatus,
    selected: OrderStatus?,
    onSelected: (OrderStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary100)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.Tune, null, tint = mutedColor())
            Text(
                selected?.label ?: "Pilih status pengerjaan...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected == null) mutedColor() else Neutral900,
                modifier = Modifier.weight(1f),
            )
            Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 280.dp),
        ) {
            TrackingUpdateViewModel.SELECTABLE.forEach { status ->
                val enabled = status.ordinal > current.ordinal
                DropdownMenuItem(
                    enabled = enabled,
                    leadingIcon = {
                        Icon(
                            when (status) {
                                OrderStatus.DITERIMA -> Icons.Filled.CheckCircle
                                OrderStatus.DIKERJAKAN -> Icons.Filled.Build
                                else -> Icons.Filled.Check
                            },
                            contentDescription = null,
                            tint = if (enabled) Primary500 else DisabledColor,
                        )
                    },
                    text = {
                        Column {
                            Text(
                                if (status == current) "${status.label} (status saat ini)" else status.label,
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                when (status) {
                                    OrderStatus.DITERIMA -> "Mitra menyetujui jadwal dan menuju lokasi"
                                    OrderStatus.DIKERJAKAN -> "Pengerjaan sedang berlangsung"
                                    else -> "Pekerjaan tuntas dan siap diserahterimakan"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = mutedColor(),
                            )
                        }
                    },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}