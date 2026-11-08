package com.example.genksuruh.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.genksuruh.core.theme.*
import com.example.genksuruh.feature.cart.components.QuantitySelector
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ============================================================
// MAIN SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemDetailScreen(
    viewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.editingItem

    if (item == null) {
        MissingCartItemContent(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
        return
    }

    key(item.id) {
        CartItemDetailContent(
            item = item,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartItemDetailContent(
    item: CartItem,
    viewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isEditingLocation by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    var showSavedBanner by remember { mutableStateOf(false) }

    val locationError = showValidationError && item.location.isBlank()
    val dateError = showValidationError && item.date.isBlank()
    val timeError = showValidationError && item.time.isBlank()

    fun handleSave() {
        val isValid = item.location.isNotBlank() &&
                item.date.isNotBlank() &&
                item.time.isNotBlank()

        if (!isValid) {
            showValidationError = true
            showSavedBanner = false
            return
        }

        viewModel.saveEditingItem()

        // Ambil kembali data terbaru supaya form tetap menampilkan
        // hasil perubahan pada layar Save Success.
        viewModel.startEditingItem(item.id)

        showValidationError = false
        showSavedBanner = true
    }

    fun handleCancel() {
        viewModel.cancelEditingItem()
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CartDetailTopBar(
                onBackClick = ::handleCancel
            )
        },
        bottomBar = {
            CartDetailBottomBar(
                onCancel = ::handleCancel,
                onSave = ::handleSave
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // FRAME: SAVE SUCCESS
                if (showSavedBanner) {
                    SaveSuccessBanner(
                        onBackToCart = ::handleCancel
                    )
                }

                // FRAME: VALIDATION ERROR
                if (showValidationError &&
                    (locationError || dateError || timeError)
                ) {
                    ValidationErrorBanner()
                }

                // 1. Informasi jasa
                ServiceInformationCard(item = item)

                // 2. Lokasi tujuan
                LocationSection(
                    location = item.location,
                    isEditing = isEditingLocation,
                    isError = locationError,
                    onEditClick = {
                        isEditingLocation = !isEditingLocation
                    },
                    onLocationChange = {
                        viewModel.updateEditingLocation(it)
                        showSavedBanner = false
                        showValidationError = false
                    }
                )

                // 3. Waktu pelaksanaan
                ScheduleSection(
                    date = item.date,
                    time = item.time,
                    dateError = dateError,
                    timeError = timeError,
                    onDateClick = {
                        showDatePicker = true
                    },
                    onTimeClick = {
                        showTimePicker = true
                    }
                )

                // 4. Catatan tambahan
                NotesSection(
                    note = item.note,
                    onNoteChange = {
                        viewModel.updateEditingNote(it.take(200))
                        showSavedBanner = false
                    }
                )

                // 5. Quantity dan rincian harga
                QuantityAndPriceSection(
                    item = item,
                    onIncrease = viewModel::increaseEditingQuantity,
                    onDecrease = viewModel::decreaseEditingQuantity
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // DATE PICKER
    if (showDatePicker) {
        DatePickerSheet(
            initialDate = parseCartDate(item.date),
            onDismiss = {
                showDatePicker = false
            },
            onConfirm = { selectedDate ->
                viewModel.updateEditingDate(formatCartDate(selectedDate))
                showDatePicker = false
                showValidationError = false
                showSavedBanner = false
            }
        )
    }

    // TIME PICKER
    if (showTimePicker) {
        TimePickerDialogContent(
            initialTime = item.time,
            onDismiss = {
                showTimePicker = false
            },
            onConfirm = { selectedTime ->
                viewModel.updateEditingTime(selectedTime)
                showTimePicker = false
                showValidationError = false
                showSavedBanner = false
            }
        )
    }
}

// ============================================================
// TOP BAR
// ============================================================

@Composable
private fun CartDetailTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "Detail Pesanan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profil",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ============================================================
// SERVICE INFORMATION CARD
// ============================================================

@Composable
private fun ServiceInformationCard(
    item: CartItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary100),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.serviceName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Primary100)
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        color = Primary700,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.providerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Penyedia terverifikasi",
                            tint = Success,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatRupiah(item.price),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary700
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "/ pesanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Primary100)
                    .padding(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Primary700
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Detail jasa ini ditetapkan oleh penyedia dan bersifat tetap.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ============================================================
// LOCATION SECTION
// ============================================================

@Composable
private fun LocationSection(
    location: String,
    isEditing: Boolean,
    isError: Boolean,
    onEditClick: () -> Unit,
    onLocationChange: (String) -> Unit
) {
    FormSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lokasi Tujuan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = " *",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Drop point terdekat kampus",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Primary100)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                color = Primary700,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(Primary100)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = location.ifBlank { "Lokasi belum diisi" },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            TextButton(onClick = onEditClick) {
                Text(
                    text = if (isEditing) "Tutup" else "Ubah",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Alamat tujuan") },
                placeholder = {
                    Text("Contoh: Gedung FILKOM Lt. 2")
                },
                singleLine = false,
                minLines = 2,
                isError = isError,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (isError) {
            Text(
                text = "Lokasi tujuan wajib diisi",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ============================================================
// SCHEDULE SECTION
// ============================================================

@Composable
private fun ScheduleSection(
    date: String,
    time: String,
    dateError: Boolean,
    timeError: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    FormSectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Waktu Pelaksanaan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = " *",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Atur kapan tugas ini harus dijalankan penyedia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateTimeField(
                label = "Tanggal",
                value = date.ifBlank { "Pilih Tanggal" },
                icon = Icons.Filled.CalendarToday,
                isError = dateError,
                onClick = onDateClick,
                modifier = Modifier.weight(1f)
            )

            DateTimeField(
                label = "Jam",
                value = time.ifBlank { "Pilih Jam" },
                icon = Icons.Filled.AccessTime,
                isError = timeError,
                onClick = onTimeClick,
                modifier = Modifier.weight(1f)
            )
        }

        if (dateError || timeError) {
            Text(
                text = buildList {
                    if (dateError) add("Tanggal wajib dipilih")
                    if (timeError) add("Waktu pelaksanaan wajib ditentukan")
                }.joinToString(" • "),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun DateTimeField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 62.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        } else {
            Primary100
        },
        shape = RoundedCornerShape(10.dp),
        border = if (isError) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error
            )
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Pilih $label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

// ============================================================
// NOTES SECTION
// ============================================================

@Composable
private fun NotesSection(
    note: String,
    onNoteChange: (String) -> Unit
) {
    FormSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Catatan untuk Penyedia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${note.length}/200",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            placeholder = {
                Text("Tambahkan instruksi khusus untuk penyedia jasa...")
            },
            minLines = 3,
            maxLines = 4,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Primary100.copy(alpha = 0.65f),
                focusedContainerColor = Primary100.copy(alpha = 0.65f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Row(
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Berikan detail pesanan seperti menu makanan, preferensi, atau petunjuk lokasi.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// QUANTITY AND PRICE SECTION
// ============================================================

@Composable
private fun QuantityAndPriceSection(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val subtotal = item.price * item.quantity

    FormSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Jumlah Pesanan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Minimal 1 pesanan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            QuantitySelector(
                quantity = item.quantity,
                onIncrease = onIncrease,
                onDecrease = onDecrease
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        PriceRow(
            label = "Harga Satuan",
            value = formatRupiah(item.price)
        )

        PriceRow(
            label = "Jumlah Pesanan",
            value = "${item.quantity}x"
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtotal",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatRupiah(subtotal),
                style = MaterialTheme.typography.titleLarge,
                color = Primary700,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================
// COMMON SECTION CARD
// ============================================================

@Composable
private fun FormSectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

// ============================================================
// BOTTOM ACTION
// ============================================================

@Composable
private fun CartDetailBottomBar(
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(0.3f)
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Primary100,
                    contentColor = Primary700
                ),
                border = null,
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            ) {
                Text(
                    text = "Batal",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(0.7f)
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary500
                ),
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Simpan Perubahan",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================
// VALIDATION AND SAVE SUCCESS
// ============================================================

@Composable
private fun ValidationErrorBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(9.dp))

            Column {
                Text(
                    text = "Data Belum Lengkap",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Mohon lengkapi lokasi tujuan dan waktu pelaksanaan sebelum menyimpan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SaveSuccessBanner(
    onBackToCart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Success.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Success
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Data pesanan berhasil diperbarui",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Success
                )

                Text(
                    text = "Baru saja",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success
                )
            }

            TextButton(onClick = onBackToCart) {
                Text(
                    text = "Kembali ke Keranjang",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success
                )
            }
        }
    }
}

// ============================================================
// DATE PICKER SHEET
// ============================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DatePickerSheet(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }

    val validInitialDate = if (initialDate.isBefore(today)) today else initialDate

    var selectedDate by remember(initialDate, today) {
        mutableStateOf(validInitialDate)
    }

    var displayedMonth by remember(initialDate, today) {
        mutableStateOf(YearMonth.from(validInitialDate))
    }

    val locale = Locale("id", "ID")

    val firstDayOffset =
        displayedMonth.atDay(1).dayOfWeek.value % 7

    val calendarDays = remember(displayedMonth) {
        buildList<LocalDate?> {
            repeat(firstDayOffset) { add(null) }

            for (day in 1..displayedMonth.lengthOfMonth()) {
                add(displayedMonth.atDay(day))
            }

            while (size < 42) {
                add(null)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pilih Tanggal Pelaksanaan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Bulan ${
                    displayedMonth.month.getDisplayName(
                        TextStyle.FULL,
                        locale
                    ).replaceFirstChar { it.titlecase(locale) }
                } ${displayedMonth.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Shortcut tanggal. FlowRow membungkus chip ke baris berikutnya
            // jika lebar layar tidak cukup, tanpa memecah teks secara vertikal.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickDateChip(
                    label = "Hari Ini",
                    date = today,
                    selected = selectedDate == today
                ) {
                    selectedDate = today
                    displayedMonth = YearMonth.from(today)
                }

                QuickDateChip(
                    label = "Besok",
                    date = today.plusDays(1),
                    selected = selectedDate == today.plusDays(1)
                ) {
                    selectedDate = today.plusDays(1)
                    displayedMonth = YearMonth.from(selectedDate)
                }

                QuickDateChip(
                    label = "Lusa",
                    date = today.plusDays(2),
                    selected = selectedDate == today.plusDays(2)
                ) {
                    selectedDate = today.plusDays(2)
                    displayedMonth = YearMonth.from(selectedDate)
                }
            }

            // Kalender
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary100.copy(alpha = 0.65f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            displayedMonth = displayedMonth.minusMonths(1)
                        }
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Bulan sebelumnya"
                        )
                    }

                    Text(
                        text = "${
                            displayedMonth.month.getDisplayName(
                                TextStyle.FULL,
                                locale
                            ).replaceFirstChar { it.titlecase(locale) }
                        } ${displayedMonth.year}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = {
                            displayedMonth = displayedMonth.plusMonths(1)
                        }
                    ) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Bulan selanjutnya"
                        )
                    }
                }

                val weekdays = listOf(
                    "Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdays.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                calendarDays.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            if (date == null) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                )
                            } else {
                                val isSelected = date == selectedDate
                                val isEnabled = !date.isBefore(today)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Primary700
                                            else Color.Transparent
                                        )
                                        .clickable(
                                            enabled = isEnabled
                                        ) {
                                            selectedDate = date
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = when {
                                            isSelected -> Color.White
                                            !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.25f
                                            )
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary100.copy(alpha = 0.7f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Penjemputan disesuaikan dengan jam operasional kampus (08.00–18.00).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.3f)
                        .height(48.dp)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = { onConfirm(selectedDate) },
                    modifier = Modifier
                        .weight(0.7f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary700
                    )
                ) {
                    Text("Pilih Tanggal")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDateChip(
    label: String,
    date: LocalDate,
    selected: Boolean,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("id", "ID"))

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = "$label (${date.format(formatter)})",
                maxLines = 1,
                softWrap = false
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary700,
            selectedLabelColor = Color.White
        )
    )
}

// ============================================================
// TIME PICKER DIALOG
// ============================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TimePickerDialogContent(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initial = parseCartTime(initialTime)

    val timeState = rememberTimePickerState(
        initialHour = initial.first,
        initialMinute = initial.second,
        // Tampilan dial memakai 1-12 dan menyediakan pilihan AM/PM.
        // Nilai state.hour tetap 0-23 sehingga bisa disimpan sebagai WIB 24 jam.
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Primary100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = Primary700
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Pilih Jam Pelaksanaan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Format 12 Jam dengan AM/PM (WIB)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TimePicker(
                    state = timeState,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Paling Sering Digunakan",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(12 to 0, 12 to 30, 13 to 0).forEach { preset ->
                        val selected = timeState.hour == preset.first &&
                                timeState.minute == preset.second

                        FilterChip(
                            selected = selected,
                            onClick = {
                                timeState.hour = preset.first
                                timeState.minute = preset.second
                            },
                            label = {
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%02d:%02d WIB",
                                        preset.first,
                                        preset.second
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val formatted = String.format(
                                Locale.US,
                                "%02d.%02d WIB",
                                timeState.hour,
                                timeState.minute
                            )
                            onConfirm(formatted)
                        },
                        modifier = Modifier
                            .weight(0.7f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary700
                        )
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Waktu")
                    }
                }
            }
        }
    }
}

// ============================================================
// EMPTY / MISSING ITEM
// ============================================================

@Composable
private fun MissingCartItemContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Item keranjang tidak ditemukan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateBack) {
            Text("Kembali ke Keranjang")
        }
    }
}

// ============================================================
// DATE / TIME HELPERS
// ============================================================

private fun parseCartDate(value: String): LocalDate {
    val locale = Locale("id", "ID")
    val patterns = listOf(
        "d MMMM yyyy",
        "d MMM yyyy"
    )

    for (pattern in patterns) {
        try {
            return LocalDate.parse(
                value,
                DateTimeFormatter.ofPattern(pattern, locale)
            )
        } catch (_: Exception) {
            // Coba format berikutnya.
        }
    }

    return LocalDate.now()
}

private fun formatCartDate(date: LocalDate): String {
    return date.format(
        DateTimeFormatter.ofPattern(
            "d MMMM yyyy",
            Locale("id", "ID")
        )
    )
}

private fun parseCartTime(value: String): Pair<Int, Int> {
    val match = Regex("""(\d{1,2})[.:](\d{2})""").find(value)

    val hour = match?.groupValues?.get(1)?.toIntOrNull() ?: 12
    val minute = match?.groupValues?.get(2)?.toIntOrNull() ?: 30

    return hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
}