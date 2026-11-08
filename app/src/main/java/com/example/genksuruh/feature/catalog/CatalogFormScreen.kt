package com.example.genksuruh.feature.catalog

import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.genksuruh.core.theme.Danger
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.theme.Success
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.genksuruh.core.theme.GenkSuruhTheme

private val TextMuted = Color(0xFF64748B)
private val FieldGray = Color(0xFFF1F5F9)
private val FormCategories = CatalogCategories.drop(1) // tanpa "Semua"
private val AreaOptions = listOf(
    "FILKOM", "Perpustakaan", "Asrama Putra", "Asrama Putri", "Kantin", "Lainnya"
)

// SCREEN: menghubungkan ViewModel, launcher galeri, dan Content
@Composable
fun CatalogFormScreen(
    onClose: () -> Unit = {},
    viewModel: CatalogFormViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onPhotoPicked(uri, context.fileSize(uri))
    }

    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    CatalogFormContent(
        state = state,
        title = viewModel.title,
        priceText = viewModel.priceText,
        description = viewModel.description,
        onTitleChange = viewModel::onTitleChange,
        onCategoryChange = viewModel::onCategoryChange,
        onPriceChange = viewModel::onPriceChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onToggleArea = viewModel::toggleArea,
        onStartTimeChange = viewModel::onStartTimeChange,
        onEndTimeChange = viewModel::onEndTimeChange,
        onPickFromGallery = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRemovePhoto = viewModel::removePhoto,
        onSave = viewModel::save,
        onDismissError = viewModel::dismissError,
        onClose = onClose
    )
}

// CONTENT: hanya menerima nilai dan lambda (launcher galeri tetap di Screen agar bisa di-preview)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogFormContent(
    state: FormUiState,
    title: String,
    priceText: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleArea: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: () -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit
) {
    // Toggle tampilan murni -> rememberSaveable (bertahan saat rotasi)
    var showSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEdit) "Edit Jasa" else "Tambah Jasa Baru",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Tutup") }
                }
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 12.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) { Text("Batal", color = TextMuted) }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary500)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Rounded.Save, null, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(8.dp))
                        Text(
                            if (state.isEdit) "Simpan Perubahan" else "Simpan Jasa",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Error simpan: input user tetap utuh, user bisa langsung coba lagi
            state.errorMessage?.let { message ->
                FormErrorBanner(message = message, onDismiss = onDismissError)
                Spacer(Modifier.height(12.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- Foto ----
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FieldLabel("Foto Jasa", required = true, modifier = Modifier.weight(1f))
                        Text("JPG/PNG, maks. 2 MB", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Spacer(Modifier.height(8.dp))
                    PhotoPicker(
                        photo = state.photo,
                        hasError = state.photoError != null,
                        onPick = { showSheet = true },
                        onRemove = onRemovePhoto
                    )
                    state.photoError?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Danger, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // ---- Judul ----
                LabeledTextField(
                    label = "Judul Jasa", required = true,
                    value = title, onValueChange = onTitleChange,
                    placeholder = "contoh: Antar Makanan Kantin FILKOM",
                    error = state.titleError, counter = "${title.length}/60"
                )

                // ---- Kategori ----
                CategoryField(
                    value = state.category, error = state.categoryError,
                    onSelected = onCategoryChange
                )

                // ---- Harga ----
                LabeledTextField(
                    label = "Harga", required = true,
                    value = priceText, onValueChange = onPriceChange,
                    placeholder = "0", prefix = "Rp ",
                    keyboardType = KeyboardType.Number,
                    error = state.priceError,
                    helper = "Tarif per sekali pesan"
                )

                // ---- Deskripsi ----
                LabeledTextField(
                    label = "Deskripsi Jasa", required = false,
                    value = description, onValueChange = onDescriptionChange,
                    placeholder = "Jelaskan detail layananmu, ketentuan, batas pesanan...",
                    minLines = 4, counter = "${description.length}/300"
                )

                // ---- Area ----
                Column {
                    Row {
                        FieldLabel("Area Layanan (Multi-pilih)", required = false, modifier = Modifier.weight(1f))
                        Text(
                            "${state.areas.size} Terpilih",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AreaOptions.forEach { area ->
                            FilterChip(
                                selected = area in state.areas,
                                onClick = { onToggleArea(area) },
                                label = { Text(area, style = MaterialTheme.typography.labelLarge) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = FieldGray,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // ---- Jam ----
                Column {
                    FieldLabel("Jam Layanan", required = false)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TimeField("Mulai", state.startTime, onStartTimeChange, Modifier.weight(1f))
                        TimeField("Selesai", state.endTime, onEndTimeChange, Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                Text("Pilih Foto", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                SheetOption(
                    icon = { Icon(Icons.Rounded.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary) },
                    text = "Pilih dari Galeri",
                    onClick = {
                        showSheet = false
                        onPickFromGallery()
                    }
                )
                SheetOption(
                    icon = { Icon(Icons.Rounded.Close, null, tint = TextMuted) },
                    text = "Batal",
                    onClick = { showSheet = false }
                )
            }
        }
    }
}

@Composable
private fun FormErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onDismiss) { Text("Tutup") }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun CatalogFormPreview() {
    GenkSuruhTheme {
        CatalogFormContent(
            state = FormUiState(titleError = "Judul wajib diisi"),
            title = "",
            priceText = "",
            description = "",
            onTitleChange = {}, onCategoryChange = {}, onPriceChange = {},
            onDescriptionChange = {}, onToggleArea = {},
            onStartTimeChange = {}, onEndTimeChange = {},
            onPickFromGallery = {}, onRemovePhoto = {}, onSave = {},
            onDismissError = {}, onClose = {}
        )
    }
}

// ================= Photo Picker (5 state) =================

@Composable
private fun PhotoPicker(
    photo: PhotoState,
    hasError: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    val isError = photo is PhotoState.Error || hasError
    val borderColor = if (isError) Danger else MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(shape)
            .background(if (isError) Danger.copy(alpha = 0.06f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .then(if (photo is PhotoState.Uploaded) Modifier else Modifier.dashedBorder(borderColor))
            .then(if (photo is PhotoState.Empty) Modifier.clickable(onClick = onPick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        when (photo) {
            is PhotoState.Empty -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ketuk untuk unggah foto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("dari galeri", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }

            is PhotoState.Uploading -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(progress = photo.progress / 100f)
                Spacer(Modifier.height(12.dp))
                Text("Mengunggah... ${photo.progress}%", style = MaterialTheme.typography.bodyMedium)
            }

            is PhotoState.Uploaded -> Box(Modifier.fillMaxSize()) {
                when {
                    photo.imageRes != null -> Image(
                        painter = painterResource(photo.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    photo.url != null -> AsyncImage(
                        model = photo.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhotoPill(Icons.Rounded.Edit, "Ganti", Color.White, onPick)
                    PhotoPill(Icons.Rounded.Close, "Hapus", Danger.copy(alpha = 0.95f), onRemove, textColor = Color.White)
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    shape = RoundedCornerShape(50),
                    color = Success
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Terunggah", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }

            is PhotoState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(Icons.Rounded.Warning, null, tint = Danger, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    photo.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Danger,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onPick) { Text("Coba Lagi") }
            }
        }
    }
}

@Composable
private fun PhotoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    background: Color,
    onClick: () -> Unit,
    textColor: Color = Color(0xFF0B1F33)
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(14.dp))
            Spacer(Modifier.size(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}

private fun Modifier.dashedBorder(color: Color) = this.drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
        ),
        cornerRadius = CornerRadius(12.dp.toPx())
    )
}

// ================= Komponen form =================

@Composable
private fun FieldLabel(text: String, required: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (required) Text(" *", style = MaterialTheme.typography.titleSmall, color = Danger)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FieldGray,
    unfocusedContainerColor = FieldGray,
    errorContainerColor = FieldGray,
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    errorBorderColor = Danger
)

@Composable
private fun LabeledTextField(
    label: String,
    required: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null,
    helper: String? = null,
    counter: String? = null,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    Column {
        FieldLabel(label, required)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextMuted) },
            prefix = prefix?.let { { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) } },
            isError = error != null,
            minLines = minLines,
            singleLine = minLines == 1,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            supportingText = if (error != null || helper != null || counter != null) {
                {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            text = error ?: helper.orEmpty(),
                            color = if (error != null) Danger else TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        if (counter != null) Text(counter, color = TextMuted)
                    }
                }
            } else null
        )
    }
}

@Composable
private fun CategoryField(value: String, error: String?, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        FieldLabel("Kategori Layanan", required = true)
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pilih kategori jasa", color = TextMuted) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, null) },
                isError = error != null,
                supportingText = error?.let { { Text(it, color = Danger) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )
            // Lapisan transparan supaya seluruh field bisa diketuk
            Box(Modifier.matchParentSize().clickable { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                FormCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { onSelected(category); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val (h, m) = value.split(":").map { it.toInt() }
                    TimePickerDialog(
                        context,
                        { _, hh, mm -> onChange("%02d:%02d".format(hh, mm)) },
                        h, m, true
                    ).show()
                },
            shape = RoundedCornerShape(12.dp),
            color = FieldGray
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Schedule, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SheetOption(icon: @Composable () -> Unit, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun Context.fileSize(uri: Uri): Long =
    contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getLong(0) else 0L
    } ?: 0L