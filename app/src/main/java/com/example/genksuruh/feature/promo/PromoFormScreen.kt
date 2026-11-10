@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.genksuruh.feature.promo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class PromoFormActions(
    val onTitleChange: (String) -> Unit,
    val onServiceChange: (String) -> Unit,
    val onCodeChange: (String) -> Unit,
    val onDiscountTypeChange: (DiscountType) -> Unit,
    val onDiscountValueChange: (String) -> Unit,
    val onMaxDiscountChange: (String) -> Unit,
    val onMinOrderChange: (String) -> Unit,
    val onQuotaChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onBannerPicked: () -> Unit,
    val onStartDateClick: () -> Unit,
    val onEndDateClick: () -> Unit,
    val onDatePicked: (Long?) -> Unit,
    val onDatePickerDismiss: () -> Unit,
    val onSaveClick: () -> Unit
)

@Composable
fun PromoFormRoute(
    viewModel: PromoFormViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaved: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSaved by rememberUpdatedState(onSaved)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PromoFormEvent.Saved -> currentOnSaved(event.message)
                is PromoFormEvent.Error -> snackbarHostState.showPromoMessage(event.message)
            }
        }
    }

    val actions = remember(viewModel) {
        PromoFormActions(
            onTitleChange = viewModel::onTitleChange,
            onServiceChange = viewModel::onServiceChange,
            onCodeChange = viewModel::onCodeChange,
            onDiscountTypeChange = viewModel::onDiscountTypeChange,
            onDiscountValueChange = viewModel::onDiscountValueChange,
            onMaxDiscountChange = viewModel::onMaxDiscountChange,
            onMinOrderChange = viewModel::onMinOrderChange,
            onQuotaChange = viewModel::onQuotaChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onBannerPicked = viewModel::onBannerPicked,
            onStartDateClick = viewModel::onStartDateClick,
            onEndDateClick = viewModel::onEndDateClick,
            onDatePicked = viewModel::onDatePicked,
            onDatePickerDismiss = viewModel::onDatePickerDismiss,
            onSaveClick = viewModel::onSaveClick
        )
    }

    PromoFormContent(
        state = state,
        snackbarHostState = snackbarHostState,
        actions = actions,
        onBack = onBack
    )
}

@Composable
fun PromoFormContent(
    state: PromoFormUiState,
    snackbarHostState: SnackbarHostState,
    actions: PromoFormActions,
    onBack: () -> Unit
) {
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) actions.onBannerPicked()
    }

    Scaffold(
        containerColor = PromoColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isNew) "Buat Promo Baru" else "Edit Promo",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PromoColors.Background
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = actions.onSaveClick,
                        enabled = !state.isSaving && !state.isLoading && !state.notFound,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PromoColors.Coral)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Menyimpan...")
                        } else {
                            Text(
                                text = if (state.isNew) "Simpan Promo" else "Simpan Perubahan",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    TextButton(
                        onClick = onBack,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.notFound -> Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Promo tidak ditemukan", color = PromoColors.TextMuted)
            }

            else -> FormBody(
                state = state,
                actions = actions,
                onPickBanner = { imagePicker.launch("image/*") },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (state.datePickerTarget != null) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = actions.onDatePickerDismiss,
            confirmButton = {
                TextButton(onClick = { actions.onDatePicked(datePickerState.selectedDateMillis) }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDatePickerDismiss) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FormBody(
    state: PromoFormUiState,
    actions: PromoFormActions,
    onPickBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(PromoColors.SoftBlue, RoundedCornerShape(12.dp))
                .dashedBorder(PromoColors.Primary)
                .clickable(onClick = onPickBanner),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = PromoColors.Primary,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = if (state.bannerPicked) "Banner dipilih ✓ (ketuk untuk ganti)"
                    else "Unggah Banner Promo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = PromoColors.Primary
                )
                Text(
                    text = "JPG/PNG, maksimal 2 MB",
                    fontSize = 11.sp,
                    color = PromoColors.TextMuted
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        PromoTextField(
            label = "Judul Promo",
            value = state.title,
            onChange = actions.onTitleChange,
            error = state.errors[FormFieldKey.TITLE],
            helper = "${state.title.length}/60"
        )

        PromoDropdownField(
            label = "Pilih Jasa Terkait",
            value = state.service,
            options = serviceOptions.map { it.name },
            error = state.errors[FormFieldKey.SERVICE],
            onSelect = actions.onServiceChange
        )

        PromoTextField(
            label = "Kode Voucher",
            value = state.code,
            onChange = actions.onCodeChange,
            error = state.errors[FormFieldKey.CODE],
            enabled = state.isNew,
            helper = if (state.isNew) "Huruf kapital dan angka, tanpa spasi"
            else "Kode tidak bisa diubah setelah dibuat"
        )

        Text("Tipe Diskon", fontSize = 12.sp, color = PromoColors.TextMuted)
        SimpleTabs(
            titles = listOf("Persen (%)", "Nominal (Rp)"),
            selected = if (state.discountType == DiscountType.PERSEN) 0 else 1,
            onSelect = { index ->
                actions.onDiscountTypeChange(
                    if (index == 0) DiscountType.PERSEN else DiscountType.NOMINAL
                )
            }
        )
        Spacer(Modifier.height(4.dp))

        PromoTextField(
            label = "Nilai Diskon",
            value = state.discountValue,
            onChange = actions.onDiscountValueChange,
            error = state.errors[FormFieldKey.VALUE],
            keyboard = KeyboardType.Number,
            prefixText = if (state.discountType == DiscountType.NOMINAL) "Rp " else null
        )

        if (state.discountType == DiscountType.PERSEN) {
            PromoTextField(
                label = "Maksimal Potongan (opsional)",
                value = state.maxDiscount,
                onChange = actions.onMaxDiscountChange,
                error = null,
                keyboard = KeyboardType.Number,
                prefixText = "Rp "
            )
        }

        PromoTextField(
            label = "Minimal Pesanan (opsional)",
            value = state.minOrder,
            onChange = actions.onMinOrderChange,
            error = null,
            keyboard = KeyboardType.Number,
            prefixText = "Rp "
        )

        PromoTextField(
            label = "Kuota Voucher",
            value = state.quota,
            onChange = actions.onQuotaChange,
            error = state.errors[FormFieldKey.QUOTA],
            keyboard = KeyboardType.Number,
            helper = "Jumlah maksimal voucher yang bisa diklaim"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PromoDateField(
                label = "Tanggal Mulai",
                value = state.startDate,
                error = state.errors[FormFieldKey.START],
                modifier = Modifier.weight(1f),
                onClick = actions.onStartDateClick
            )
            PromoDateField(
                label = "Tanggal Berakhir",
                value = state.endDate,
                error = state.errors[FormFieldKey.END],
                modifier = Modifier.weight(1f),
                onClick = actions.onEndDateClick
            )
        }

        PromoTextField(
            label = "Deskripsi",
            value = state.description,
            onChange = actions.onDescriptionChange,
            error = null,
            singleLine = false,
            helper = "${state.description.length}/300"
        )
    }
}

@Composable
private fun PromoTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: String?,
    keyboard: KeyboardType = KeyboardType.Text,
    prefixText: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    helper: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { Text(error ?: helper.orEmpty()) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        enabled = enabled,
        prefix = if (prefixText != null) {
            { Text(prefixText) }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PromoDateField(
    label: String,
    value: String,
    error: String?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(modifier) {
        OutlinedTextField(
            value = if (value.isBlank()) "" else formatTanggal(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            isError = error != null,
            supportingText = { Text(error.orEmpty()) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun PromoDropdownField(
    label: String,
    value: String,
    options: List<String>,
    error: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            isError = error != null,
            supportingText = { Text(error.orEmpty()) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}