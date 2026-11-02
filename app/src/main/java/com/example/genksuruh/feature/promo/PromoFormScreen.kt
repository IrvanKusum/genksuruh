package com.example.genksuruh.feature.promo

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoFormScreen(promoId: String, vm: PromoViewModel, onBack: () -> Unit) {
    val isNew = promoId == "new"
    val promos by vm.promos.collectAsState()
    val existing = if (isNew) null else promos.find { it.id == promoId }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var service by remember { mutableStateOf(existing?.serviceName ?: "") }
    var code by remember { mutableStateOf(existing?.code ?: "") }
    var type by remember { mutableStateOf(existing?.discountType ?: DiscountType.PERSEN) }
    var value by remember { mutableStateOf(existing?.discountValue?.toString() ?: "") }
    var quota by remember { mutableStateOf(existing?.quota?.toString() ?: "") }
    var startDate by remember { mutableStateOf(existing?.startDate ?: "") }
    var endDate by remember { mutableStateOf(existing?.endDate ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }

    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var bannerError by remember { mutableStateOf(false) }

    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var saving by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableIntStateOf(0) } // 0 = tutup, 1 = mulai, 2 = berakhir

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val size = runCatching {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            }.getOrNull() ?: 0L
            if (size > 2L * 1024 * 1024) {
                bannerError = true
                bannerUri = null
            } else {
                bannerError = false
                bannerUri = uri
            }
        }
    }

    // Decode gambar kecil untuk preview (upload ke Supabase Storage menyusul)
    val bannerBitmap by produceState<ImageBitmap?>(initialValue = null, bannerUri) {
        val uri = bannerUri
        this.value = if (uri == null) null else withContext(Dispatchers.IO) {
            runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                context.contentResolver.openInputStream(uri)?.use { s ->
                    BitmapFactory.decodeStream(s, null, opts)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    fun validate(): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (title.isBlank()) e["title"] = "Judul promo wajib diisi"
        if (service.isBlank()) e["service"] = "Pilih jasa terkait"
        val c = code.trim().uppercase()
        if (c.isBlank()) e["code"] = "Kode voucher wajib diisi"
        else if (promos.any { it.code.equals(c, true) && it.id != existing?.id }) {
            e["code"] = "Kode voucher sudah dipakai"
        }
        val v = value.toIntOrNull()
        if (v == null || v <= 0) e["value"] = "Nilai diskon wajib diisi"
        else if (type == DiscountType.PERSEN && v > 100) e["value"] = "Maksimal 100%"
        val q = quota.toIntOrNull()
        if (q == null || q < 1) e["quota"] = "Kuota minimal 1"
        else if (existing != null && q < existing.claimed) {
            e["quota"] = "Kuota tidak boleh kurang dari ${existing.claimed} (sudah diklaim)"
        }
        if (startDate.isBlank()) e["start"] = "Pilih tanggal mulai"
        if (endDate.isBlank()) e["end"] = "Pilih tanggal berakhir"
        else if (startDate.isNotBlank() && endDate < startDate) {
            e["end"] = "Tanggal berakhir harus setelah tanggal mulai"
        }
        return e
    }

    fun buildPromo(): Promo {
        val svc = serviceOptions.find { it.name == service }
        return Promo(
            id = existing?.id ?: "p${System.currentTimeMillis()}",
            title = title.trim(),
            code = code.trim().uppercase(),
            category = svc?.category ?: existing?.category ?: "Lainnya",
            emoji = svc?.emoji ?: existing?.emoji ?: "🎟️",
            discountType = type,
            discountValue = value.toInt(),
            maxDiscount = if (type == DiscountType.PERSEN) existing?.maxDiscount ?: 0 else 0,
            minOrder = existing?.minOrder ?: 0,
            serviceName = service,
            providerName = existing?.providerName ?: PromoRepository.CURRENT_PROVIDER_NAME,
            ownerId = existing?.ownerId ?: PromoRepository.CURRENT_PROVIDER_ID,
            startDate = startDate,
            endDate = endDate,
            quota = quota.toInt(),
            claimed = existing?.claimed ?: 0,
            description = description.trim(),
            terms = existing?.terms ?: defaultTerms
        )
    }

    Scaffold(
        containerColor = PromoColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isNew) "Buat Promo Baru" else "Edit Promo",
                        fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PromoColors.Background)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val e = validate()
                            errors = e
                            if (e.isEmpty() && !saving) {
                                saving = true
                                scope.launch {
                                    delay(1200) // simulasi loading simpan
                                    vm.savePromo(buildPromo(), isNew)
                                    onBack()
                                }
                            }
                        },
                        enabled = !saving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PromoColors.Coral,
                            disabledContainerColor = PromoColors.Coral.copy(alpha = 0.55f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Menyimpan...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(
                                if (isNew) "Simpan Promo" else "Simpan Perubahan",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BannerUploadBox(
                bitmap = bannerBitmap,
                hasBanner = bannerUri != null,
                error = bannerError,
                onPick = { imagePicker.launch("image/*") },
                onRemove = { bannerUri = null; bannerError = false }
            )

            LabeledField(
                "Judul Promo", title, { if (it.length <= 60) title = it },
                placeholder = "Contoh: Hemat 50% Antar Makanan",
                error = errors["title"], helper = "${title.length}/60"
            )

            PickerField(
                label = "Pilih Jasa Terkait",
                value = service,
                placeholder = "Pilih jasa",
                error = errors["service"],
                trailing = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) }
            ) { open, dismiss ->
                DropdownMenu(expanded = open, onDismissRequest = dismiss) {
                    serviceOptions.forEach { o ->
                        DropdownMenuItem(
                            text = { Text(o.name) },
                            onClick = { service = o.name; dismiss() }
                        )
                    }
                }
            }

            LabeledField(
                "Kode Voucher", code,
                { code = it.uppercase().filter { ch -> ch.isLetterOrDigit() } },
                placeholder = "Contoh: GENKPERTAMA",
                error = errors["code"],
                enabled = isNew,
                helper = if (isNew) "Huruf kapital dan angka, tanpa spasi"
                else "Kode tidak bisa diubah setelah dibuat",
                trailing = if (isNew) {
                    {
                        TextButton(onClick = { code = "PROMO${(100..999).random()}" }) {
                            Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else null
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Tipe Diskon")
                SegmentedToggle(
                    listOf("Persen (%)", "Nominal (Rp)"),
                    if (type == DiscountType.PERSEN) 0 else 1
                ) { type = if (it == 0) DiscountType.PERSEN else DiscountType.NOMINAL }
            }

            LabeledField(
                "Nilai Diskon", value, { if (it.all(Char::isDigit)) value = it },
                placeholder = if (type == DiscountType.PERSEN) "Contoh: 50" else "Contoh: 5000",
                error = errors["value"], keyboard = KeyboardType.Number,
                prefixText = if (type == DiscountType.NOMINAL) "Rp " else null,
                suffixText = if (type == DiscountType.PERSEN) "%" else null
            )

            LabeledField(
                "Kuota Voucher", quota, { if (it.all(Char::isDigit)) quota = it },
                placeholder = "Contoh: 100",
                error = errors["quota"], keyboard = KeyboardType.Number,
                helper = "Jumlah maksimal voucher yang bisa diklaim"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PickerField(
                    label = "Tanggal Mulai",
                    value = if (startDate.isBlank()) "" else formatTanggal(startDate),
                    placeholder = "Pilih",
                    error = errors["start"],
                    trailing = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    onClickOverride = { pickerTarget = 1 }
                ) { _, _ -> }
                PickerField(
                    label = "Tanggal Berakhir",
                    value = if (endDate.isBlank()) "" else formatTanggal(endDate),
                    placeholder = "Pilih",
                    error = errors["end"],
                    trailing = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    onClickOverride = { pickerTarget = 2 }
                ) { _, _ -> }
            }

            LabeledField(
                "Deskripsi", description, { if (it.length <= 300) description = it },
                placeholder = "Ceritakan promo ini",
                singleLine = false, helper = "${description.length}/300"
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (pickerTarget != 0) {
        val dpState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pickerTarget = 0 },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { ms ->
                        val iso = millisToIso(ms)
                        if (pickerTarget == 1) startDate = iso else endDate = iso
                    }
                    pickerTarget = 0
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickerTarget = 0 }) { Text("Batal") } }
        ) { DatePicker(state = dpState) }
    }
}

// ======================= Komponen form =======================

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PromoColors.TextDark)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PromoColors.Primary,
    unfocusedBorderColor = PromoColors.Outline,
    errorBorderColor = PromoColors.Kedaluwarsa,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,
    disabledContainerColor = Color(0xFFEEF2F5),
    disabledBorderColor = PromoColors.Outline,
    disabledTextColor = PromoColors.TextMuted
)

private fun SupportText(error: String?, helper: String?): (@Composable () -> Unit)? =
    if (error != null || helper != null) {
        {
            Text(
                error ?: helper.orEmpty(),
                fontSize = 11.sp,
                color = if (error != null) PromoColors.Kedaluwarsa else PromoColors.TextMuted
            )
        }
    } else null

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "",
    error: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    prefixText: String? = null,
    suffixText: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    helper: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = PromoColors.TextMuted) },
            isError = error != null,
            supportingText = SupportText(error, helper),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            prefix = if (prefixText != null) { { Text(prefixText) } } else null,
            suffix = if (suffixText != null) { { Text(suffixText) } } else null,
            trailingIcon = trailing,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Field baca-saja yang bisa diketuk: dropdown atau tanggal. */
@Composable
private fun PickerField(
    label: String,
    value: String,
    placeholder: String,
    error: String?,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClickOverride: (() -> Unit)? = null,
    menu: @Composable (open: Boolean, dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(placeholder, fontSize = 13.sp, color = PromoColors.TextMuted) },
                trailingIcon = trailing,
                isError = error != null,
                supportingText = SupportText(error, null),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { if (onClickOverride != null) onClickOverride() else expanded = true }
            )
            menu(expanded) { expanded = false }
        }
    }
}

@Composable
private fun BannerUploadBox(
    bitmap: ImageBitmap?,
    hasBanner: Boolean,
    error: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    val borderColor = if (error) PromoColors.Kedaluwarsa else PromoColors.Primary
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (error) Color(0xFFFDECEC) else PromoColors.SoftBlue)
            .dashedBorder(borderColor)
            .clickable(enabled = !hasBanner, onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        when {
            error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Warning, contentDescription = null,
                    tint = PromoColors.Kedaluwarsa, modifier = Modifier.size(32.dp)
                )
                Text(
                    "Gagal mengunggah. Ukuran file melebihi 2 MB.",
                    fontSize = 12.sp, color = PromoColors.Kedaluwarsa,
                    fontWeight = FontWeight.Medium
                )
                OutlinedButton(
                    onClick = onPick,
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) { Text("Coba Lagi", fontSize = 11.sp) }
            }

            hasBanner -> {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap, contentDescription = "Banner promo",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                }
                Row(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallRoundIcon(Icons.Outlined.Edit, "Ganti", onPick)
                    SmallRoundIcon(Icons.Outlined.Close, "Hapus", onRemove)
                }
                Text(
                    "Terunggah", fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    color = PromoColors.Aktif,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    Icons.Outlined.Image, contentDescription = null,
                    tint = PromoColors.Primary, modifier = Modifier.size(36.dp)
                )
                Text(
                    "Unggah Banner Promo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = PromoColors.Primary
                )
                Text("JPG/PNG, maksimal 2 MB", fontSize = 11.sp, color = PromoColors.TextMuted)
            }
        }
    }
}

@Composable
private fun SmallRoundIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(32.dp)
            .background(Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = description,
            tint = PromoColors.TextDark, modifier = Modifier.size(16.dp)
        )
    }
}