package com.example.genksuruh.feature.promo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormFieldKey { TITLE, SERVICE, CODE, VALUE, QUOTA, START, END }

enum class DatePickerTarget { START, END }

data class PromoFormUiState(
    val isNew: Boolean = true,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val isSaving: Boolean = false,

    val title: String = "",
    val service: String = "",
    val code: String = "",
    val discountType: DiscountType = DiscountType.PERSEN,
    val discountValue: String = "",
    val maxDiscount: String = "",
    val minOrder: String = "",
    val quota: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val description: String = "",
    val bannerPicked: Boolean = false,

    val errors: Map<FormFieldKey, String> = emptyMap(),
    val datePickerTarget: DatePickerTarget? = null
)

sealed interface PromoFormEvent {
    data class Saved(val message: String) : PromoFormEvent
    data class Error(val message: String) : PromoFormEvent
}

class PromoFormViewModel(
    private val promoId: String,
    private val repository: PromoRepository,
    private val session: SessionRepository
) : ViewModel() {

    companion object {
        const val NEW_ID = "new"
    }

    private val isNew = promoId == NEW_ID
    private var existing: Promo? = null

    private val _uiState = MutableStateFlow(PromoFormUiState(isNew = isNew, isLoading = !isNew))
    val uiState: StateFlow<PromoFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<PromoFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (!isNew) loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            val promo = repository.getPromoById(promoId)
            if (promo == null) {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
                return@launch
            }
            existing = promo
            _uiState.update { s ->
                s.copy(
                    isLoading = false,
                    title = promo.title,
                    service = promo.serviceName,
                    code = promo.code,
                    discountType = promo.discountType,
                    discountValue = promo.discountValue.toString(),
                    maxDiscount = promo.maxDiscount.takeIf { it > 0 }?.toString().orEmpty(),
                    minOrder = promo.minOrder.takeIf { it > 0 }?.toString().orEmpty(),
                    quota = promo.quota.toString(),
                    startDate = promo.startDate,
                    endDate = promo.endDate,
                    description = promo.description,
                    bannerPicked = true
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        if (value.length > 60) return
        _uiState.update { it.copy(title = value, errors = it.errors - FormFieldKey.TITLE) }
    }

    fun onServiceChange(value: String) {
        _uiState.update { it.copy(service = value, errors = it.errors - FormFieldKey.SERVICE) }
    }

    fun onCodeChange(value: String) {
        if (!isNew) return
        val cleaned = value.uppercase().filter { it.isLetterOrDigit() }
        _uiState.update { it.copy(code = cleaned, errors = it.errors - FormFieldKey.CODE) }
    }

    fun onDiscountTypeChange(type: DiscountType) {
        _uiState.update { it.copy(discountType = type, errors = it.errors - FormFieldKey.VALUE) }
    }

    fun onDiscountValueChange(value: String) {
        if (value.length > 9 || !value.all { it.isDigit() }) return
        _uiState.update { it.copy(discountValue = value, errors = it.errors - FormFieldKey.VALUE) }
    }

    fun onMaxDiscountChange(value: String) {
        if (value.length > 9 || !value.all { it.isDigit() }) return
        _uiState.update { it.copy(maxDiscount = value) }
    }

    fun onMinOrderChange(value: String) {
        if (value.length > 9 || !value.all { it.isDigit() }) return
        _uiState.update { it.copy(minOrder = value) }
    }

    fun onQuotaChange(value: String) {
        if (value.length > 9 || !value.all { it.isDigit() }) return
        _uiState.update { it.copy(quota = value, errors = it.errors - FormFieldKey.QUOTA) }
    }

    fun onDescriptionChange(value: String) {
        if (value.length > 300) return
        _uiState.update { it.copy(description = value) }
    }

    fun onBannerPicked() {
        _uiState.update { it.copy(bannerPicked = true) }
    }

    fun onStartDateClick() {
        _uiState.update { it.copy(datePickerTarget = DatePickerTarget.START) }
    }

    fun onEndDateClick() {
        _uiState.update { it.copy(datePickerTarget = DatePickerTarget.END) }
    }

    fun onDatePickerDismiss() {
        _uiState.update { it.copy(datePickerTarget = null) }
    }

    fun onDatePicked(millis: Long?) {
        val iso = millis?.let { millisToIso(it) }
        _uiState.update { s ->
            val target = s.datePickerTarget
            if (iso == null || target == null) {
                s.copy(datePickerTarget = null)
            } else if (target == DatePickerTarget.START) {
                s.copy(
                    startDate = iso,
                    datePickerTarget = null,
                    errors = s.errors - FormFieldKey.START - FormFieldKey.END
                )
            } else {
                s.copy(
                    endDate = iso,
                    datePickerTarget = null,
                    errors = s.errors - FormFieldKey.END
                )
            }
        }
    }

    fun onSaveClick() {
        val snapshot = _uiState.value
        if (snapshot.isSaving || snapshot.isLoading || snapshot.notFound) return

        val errors = validate(snapshot)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errors = emptyMap()) }

            val code = snapshot.code.trim().uppercase()
            if (repository.isCodeTaken(code, existing?.id)) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errors = mapOf(FormFieldKey.CODE to "Kode voucher sudah dipakai")
                    )
                }
                return@launch
            }

            delay(1200)
            repository.savePromo(buildPromo(snapshot))
                .onSuccess {
                    _events.trySend(
                        PromoFormEvent.Saved(
                            if (isNew) "Promo berhasil dibuat" else "Perubahan disimpan"
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.update { s -> s.copy(isSaving = false) }
                    _events.trySend(
                        PromoFormEvent.Error(error.message ?: "Gagal menyimpan promo")
                    )
                }
        }
    }

    private fun validate(s: PromoFormUiState): Map<FormFieldKey, String> {
        val errors = mutableMapOf<FormFieldKey, String>()
        val current = existing

        if (s.title.isBlank()) errors[FormFieldKey.TITLE] = "Judul promo wajib diisi"
        if (s.service.isBlank()) errors[FormFieldKey.SERVICE] = "Pilih jasa terkait"
        if (s.code.isBlank()) errors[FormFieldKey.CODE] = "Kode voucher wajib diisi"

        val value = s.discountValue.toIntOrNull()
        if (value == null || value <= 0) {
            errors[FormFieldKey.VALUE] = "Nilai diskon wajib diisi"
        } else if (s.discountType == DiscountType.PERSEN && value > 100) {
            errors[FormFieldKey.VALUE] = "Maksimal 100%"
        }

        val quota = s.quota.toIntOrNull()
        if (quota == null || quota < 1) {
            errors[FormFieldKey.QUOTA] = "Kuota minimal 1"
        } else if (current != null && quota < current.claimed) {
            errors[FormFieldKey.QUOTA] =
                "Kuota tidak boleh kurang dari ${current.claimed} (sudah diklaim)"
        }

        if (s.startDate.isBlank()) errors[FormFieldKey.START] = "Pilih tanggal mulai"
        if (s.endDate.isBlank()) {
            errors[FormFieldKey.END] = "Pilih tanggal berakhir"
        } else if (s.startDate.isNotBlank() && s.endDate < s.startDate) {
            errors[FormFieldKey.END] = "Tanggal berakhir harus setelah tanggal mulai"
        }
        return errors
    }

    private fun buildPromo(s: PromoFormUiState): Promo {
        val service = serviceOptions.find { it.name == s.service }
        val current = existing
        return Promo(
            id = current?.id ?: "p${System.currentTimeMillis()}",
            title = s.title.trim(),
            code = s.code.trim().uppercase(),
            category = service?.category ?: "Lainnya",
            emoji = service?.emoji ?: "🎟️",
            discountType = s.discountType,
            discountValue = s.discountValue.toInt(),
            maxDiscount = if (s.discountType == DiscountType.PERSEN) {
                s.maxDiscount.toIntOrNull() ?: 0
            } else {
                0
            },
            minOrder = s.minOrder.toIntOrNull() ?: 0,
            serviceName = s.service,
            providerName = current?.providerName ?: session.currentProvider,
            startDate = s.startDate,
            endDate = s.endDate,
            quota = s.quota.toInt(),
            claimed = current?.claimed ?: 0,
            description = s.description.trim(),
            terms = current?.terms ?: defaultTerms
        )
    }
}