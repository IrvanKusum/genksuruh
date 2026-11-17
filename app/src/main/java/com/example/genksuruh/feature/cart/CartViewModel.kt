package com.example.genksuruh.feature.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.genksuruh.core.auth.AuthRepository
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.network.SupabaseClient
import com.example.genksuruh.feature.promo.DiscountType
import com.example.genksuruh.feature.promo.PromoModule
import com.example.genksuruh.feature.promo.effectiveStatus
import com.example.genksuruh.feature.tracking.FakeTrackingRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import com.example.genksuruh.feature.promo.VoucherStatus as ClaimStatus

/**
 * Hasil proses checkout.
 */
enum class CheckoutResult {
    SUCCESS,
    FAILED,
    INSUFFICIENT_BALANCE
}

private fun getTodayFormattedDate(): String {
    val locale = Locale("id", "ID")
    return try {
        LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
    } catch (_: Exception) {
        SimpleDateFormat("d MMMM yyyy", locale).format(Date())
    }
}

/**
 * ViewModel fitur Keranjang dan Checkout.
 */
class CartViewModel(
    private val repository: CartRepository = CartRepository.shared
) : ViewModel() {

    // =====================================================
    // UI STATE
    // =====================================================

    private val _screenState = MutableStateFlow(CartUiState())

    val uiState: StateFlow<CartUiState> = combine(
        repository.cartItems,
        AuthRepository.account,
        _screenState
    ) { cartItems, account, screenState ->

        screenState.copy(
            items = cartItems,
            watuPayBalance = account?.watupayBalance ?: screenState.watuPayBalance
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CartUiState()
    )

    init {
        loadCart()
        loadAvailableVouchers()
    }

    // =====================================================
    // CART
    // =====================================================

    fun loadCart() {
        viewModelScope.launch {
            _screenState.update {
                it.copy(
                    isLoading = true,
                    isError = false,
                    errorMessage = null
                )
            }

            try {
                repository.loadCart()
                loadAvailableVouchers()

                _screenState.update {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        errorMessage = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _screenState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message
                            ?: CartDummyData.GENERIC_ERROR_MESSAGE
                    )
                }
            }
        }
    }

    fun loadAvailableVouchers() {
        viewModelScope.launch {
            runCatching {
                val currentAccount = AuthRepository.account.value
                val currentUserId = currentAccount?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id.orEmpty()
                val currentUserName = currentAccount?.name ?: PromoModule.session.currentUser

                val promoRepo = PromoModule.repository
                val promos = promoRepo.promos.first()
                val claims = promoRepo.claims.first()

                val activeClaimedPromoIds = claims
                    .filter {
                        (it.userId == currentUserId || it.userId.isBlank() || it.userName.equals(currentUserName, ignoreCase = true)) &&
                                it.status == ClaimStatus.AKTIF
                    }
                    .map { it.promoId }
                    .toSet()

                val available = promos.filter { it.id in activeClaimedPromoIds && !it.isExpired }
                _screenState.update { it.copy(availableVouchers = available) }
            }.onFailure { e ->
                Log.e("CartViewModel", "loadAvailableVouchers gagal: ${e.message}", e)
            }
        }
    }

    fun simulateLoadError() {
        repository.simulateNextLoadFailure()
        loadCart()
    }

    fun toggleItemSelection(itemId: String) {
        repository.toggleItemSelection(itemId)
        invalidateVoucherAfterCartChange()
    }

    fun selectOnly(itemId: String) {
        repository.selectOnly(itemId)
        invalidateVoucherAfterCartChange()
    }

    fun selectAllItems() {
        repository.selectAllItems()
        invalidateVoucherAfterCartChange()
    }

    fun increaseQuantity(itemId: String) {}

    fun decreaseQuantity(itemId: String) {}

    // =====================================================
    // EDIT & DRAFT DETAIL PESANAN
    // =====================================================

    /** Variasi A: Klik "Pesan" -> Direct Order tanpa simpan ke cart_items dahulu */
    fun prepareDirectOrder(service: Service) {
        val uid = AuthRepository.account.value?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id.orEmpty()
        val todayStr = getTodayFormattedDate()
        val draft = CartItem(
            id = UUID.randomUUID().toString(),
            serviceId = service.id,
            userId = uid,
            serviceName = service.title,
            category = service.category,
            providerName = service.providerName,
            price = service.price,
            imageUrl = service.imageUrl,
            location = "Area Kampus UB",
            date = todayStr,
            time = "12.00 WIB",
            note = "",
            quantity = 1,
            isSelected = true
        )
        _screenState.update {
            it.copy(
                editingItem = draft,
                detailMode = CartDetailMode.DIRECT_ORDER,
                directOrderDraft = draft
            )
        }
        resetCheckoutState()
    }

    /** Variasi B: Klik "Tambahkan ke Keranjang" -> isi detail dulu baru simpan ke cart_items */
    fun prepareAddToCart(service: Service) {
        val uid = AuthRepository.account.value?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id.orEmpty()
        val todayStr = getTodayFormattedDate()
        val draft = CartItem(
            id = UUID.randomUUID().toString(),
            serviceId = service.id,
            userId = uid,
            serviceName = service.title,
            category = service.category,
            providerName = service.providerName,
            price = service.price,
            imageUrl = service.imageUrl,
            location = "Area Kampus UB",
            date = todayStr,
            time = "12.00 WIB",
            note = "",
            quantity = 1,
            isSelected = true
        )
        _screenState.update {
            it.copy(
                editingItem = draft,
                detailMode = CartDetailMode.ADD_TO_CART,
                directOrderDraft = null
            )
        }
    }

    /** Variasi C: Klik "Edit" pada item di Keranjang */
    fun startEditingItem(itemId: String) {
        val item = repository.getItemById(itemId) ?: return

        _screenState.update {
            it.copy(
                editingItem = item.copy(),
                detailMode = CartDetailMode.EDIT_CART_ITEM,
                directOrderDraft = null
            )
        }
    }

    fun updateEditingLocation(location: String) {
        updateEditingItem { item -> item.copy(location = location) }
    }

    fun updateEditingDate(date: String) {
        updateEditingItem { item -> item.copy(date = date) }
    }

    fun updateEditingTime(time: String) {
        updateEditingItem { item -> item.copy(time = time) }
    }

    fun updateEditingNote(note: String) {
        updateEditingItem { item -> item.copy(note = note) }
    }

    fun updateEditingQuantity(quantity: Int) {}

    fun increaseEditingQuantity() {}

    fun decreaseEditingQuantity() {}

    private fun updateEditingItem(
        transform: (CartItem) -> CartItem
    ) {
        _screenState.update { state ->
            val item = state.editingItem ?: return@update state
            val updated = transform(item)
            state.copy(
                editingItem = updated,
                directOrderDraft = if (state.detailMode == CartDetailMode.DIRECT_ORDER) updated else state.directOrderDraft
            )
        }
    }

    /** Eksekusi Simpan Variasi B (INSERT ke cart_items) */
    fun confirmAddToCart(onSuccess: () -> Unit) {
        val draft = _screenState.value.editingItem ?: return
        repository.addCartItemWithDetails(draft)
        _screenState.update {
            it.copy(
                editingItem = null,
                userNotificationMessage = "Pesanan berhasil disimpan di keranjang"
            )
        }
        onSuccess()
    }

    /** Eksekusi Simpan Variasi C (UPDATE cart_items) */
    fun confirmEditCartItem(onSuccess: () -> Unit) {
        val editedItem = _screenState.value.editingItem ?: return
        repository.updateItem(editedItem)
        _screenState.update {
            it.copy(
                editingItem = null,
                userNotificationMessage = "Detail pesanan berhasil diedit"
            )
        }
        invalidateVoucherAfterCartChange()
        onSuccess()
    }

    /** Eksekusi Variasi A (Direct Checkout) */
    fun confirmDirectOrder(onSuccess: () -> Unit) {
        val draft = _screenState.value.editingItem ?: return
        _screenState.update {
            it.copy(
                editingItem = null,
                directOrderDraft = draft
            )
        }
        resetCheckoutState()
        onSuccess()
    }

    fun dismissNotificationMessage() {
        _screenState.update { it.copy(userNotificationMessage = null) }
    }

    fun saveEditingItem() {
        val editedItem = _screenState.value.editingItem ?: return
        repository.updateItem(editedItem)
        _screenState.update { it.copy(editingItem = null) }
        invalidateVoucherAfterCartChange()
    }

    fun cancelEditingItem() {
        _screenState.update {
            it.copy(editingItem = null)
        }
    }

    // =====================================================
    // DELETE + UNDO
    // =====================================================

    fun removeItem(itemId: String) {
        val item = repository.getItemById(itemId) ?: return

        _screenState.update {
            it.copy(
                itemPendingDelete = item,
                showDeleteDialog = true
            )
        }
    }

    fun dismissDeleteDialog() {
        _screenState.update {
            it.copy(
                itemPendingDelete = null,
                showDeleteDialog = false
            )
        }
    }

    fun confirmRemoveItem() {
        val target = _screenState.value.itemPendingDelete ?: return

        val removed = repository.removeItem(target.id)

        _screenState.update {
            it.copy(
                itemPendingDelete = null,
                showDeleteDialog = false,
                lastRemovedItem = removed?.item,
                lastRemovedItemIndex = removed?.index ?: -1,
                showUndoSnackbar = removed != null
            )
        }

        if (removed != null) {
            invalidateVoucherAfterCartChange()
        }
    }

    fun undoRemoveItem() {
        val current = _screenState.value
        val removedItem = current.lastRemovedItem ?: return

        repository.restoreItem(
            item = removedItem,
            index = current.lastRemovedItemIndex
        )

        _screenState.update {
            it.copy(
                lastRemovedItem = null,
                lastRemovedItemIndex = -1,
                showUndoSnackbar = false
            )
        }

        invalidateVoucherAfterCartChange()
    }

    fun dismissUndoSnackbar() {
        _screenState.update {
            it.copy(
                showUndoSnackbar = false,
                lastRemovedItem = null,
                lastRemovedItemIndex = -1
            )
        }
    }

    // =====================================================
    // VOUCHER
    // =====================================================

    fun updateVoucherCode(code: String) {
        _screenState.update {
            it.copy(
                voucherCode = code.take(30),
                voucherStatus = if (code.isBlank()) {
                    VoucherStatus.IDLE
                } else {
                    VoucherStatus.INPUT
                },
                voucherDiscountAmount = 0L,
                voucherClaimId = null,
                voucherLabel = "",
                voucherMessage = null
            )
        }
    }

    fun applyVoucher() {
        val initial = uiState.value
        val code = initial.voucherCode.trim().uppercase()

        if (code.isBlank() || initial.voucherStatus == VoucherStatus.VALIDATING) return

        _screenState.update {
            it.copy(
                voucherCode = code,
                voucherStatus = VoucherStatus.VALIDATING,
                voucherDiscountAmount = 0L,
                voucherMessage = null,
                voucherClaimId = null,
                voucherLabel = ""
            )
        }

        viewModelScope.launch {
            delay(400L)

            val latest = uiState.value
            if (
                latest.voucherCode.trim().uppercase() != code ||
                latest.voucherStatus != VoucherStatus.VALIDATING
            ) return@launch

            try {
                val currentAccount = AuthRepository.account.value
                val currentUserId = currentAccount?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id.orEmpty()
                val currentUserName = currentAccount?.name ?: PromoModule.session.currentUser

                val promoRepo = PromoModule.repository
                val promos = promoRepo.promos.first()
                val claims = promoRepo.claims.first()

                val promo = promos.find { it.code.trim().equals(code, ignoreCase = true) }

                val claim = promo?.let { p ->
                    claims.find {
                        it.promoId == p.id &&
                                (it.userId == currentUserId || it.userId.isBlank() || it.userName.equals(currentUserName, ignoreCase = true)) &&
                                it.effectiveStatus(p) == ClaimStatus.AKTIF
                    }
                }

                fun fail(status: VoucherStatus, message: String, minimum: Long? = null) {
                    _screenState.update {
                        it.copy(
                            voucherStatus = status,
                            voucherDiscountAmount = 0L,
                            voucherMessage = message,
                            voucherMinimumTransaction = minimum ?: it.voucherMinimumTransaction
                        )
                    }
                }

                when {
                    promo == null ->
                        fail(VoucherStatus.NOT_FOUND, "Kode voucher \"$code\" tidak ditemukan.")

                    promo.isExpired ->
                        fail(VoucherStatus.NOT_FOUND, "Promo \"${promo.title}\" sudah berakhir.")

                    claim == null ->
                        fail(
                            VoucherStatus.NOT_FOUND,
                            "Voucher \"${promo.code}\" belum kamu klaim atau sudah terpakai. Klaim dulu di menu Promo."
                        )

                    else -> {
                        // Kategori check
                        val isUniversalCategory = promo.category.isBlank() ||
                                promo.category.equals("Semua Layanan", ignoreCase = true) ||
                                promo.category.equals("Semua", ignoreCase = true) ||
                                promo.category.equals("Lainnya", ignoreCase = true)

                        val eligible = if (isUniversalCategory) {
                            latest.selectedItems
                        } else {
                            latest.selectedItems.filter {
                                it.category.equals(promo.category, ignoreCase = true) ||
                                        it.serviceName.equals(promo.serviceName, ignoreCase = true) ||
                                        (promo.serviceId != null && promo.serviceId == it.serviceId)
                            }
                        }

                        if (eligible.isEmpty()) {
                            fail(
                                VoucherStatus.NOT_FOUND,
                                "Voucher ini hanya berlaku untuk kategori/layanan \"${promo.category.ifBlank { promo.serviceName }}\"."
                            )
                            return@launch
                        }

                        val base = eligible.sumOf { it.lineTotal }

                        if (base < promo.minOrder) {
                            fail(
                                VoucherStatus.MINIMUM_NOT_MET,
                                "Minimal transaksi ${formatRupiah(promo.minOrder.toLong())} untuk menggunakan voucher ini.",
                                promo.minOrder.toLong()
                            )
                            return@launch
                        }

                        val raw = when (promo.discountType) {
                            DiscountType.PERSEN -> base * promo.discountValue / 100L
                            DiscountType.NOMINAL -> promo.discountValue.toLong()
                        }
                        val capped =
                            if (promo.discountType == DiscountType.PERSEN && promo.maxDiscount > 0)
                                raw.coerceAtMost(promo.maxDiscount.toLong())
                            else raw
                        val discount = capped.coerceAtMost(base)

                        _screenState.update {
                            it.copy(
                                voucherStatus = VoucherStatus.APPLIED,
                                voucherDiscountAmount = discount,
                                voucherMinimumTransaction = promo.minOrder.toLong(),
                                voucherClaimId = claim.id,
                                voucherLabel =
                                    if (promo.discountType == DiscountType.PERSEN)
                                        "Diskon ${promo.discountValue}%"
                                    else "Potongan ${formatRupiah(promo.discountValue.toLong())}",
                                voucherMessage = "Voucher ${promo.code} berhasil diterapkan!"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "applyVoucher error: ${e.message}", e)
                _screenState.update {
                    it.copy(
                        voucherStatus = VoucherStatus.NOT_FOUND,
                        voucherMessage = e.message ?: "Gagal memvalidasi voucher."
                    )
                }
            }
        }
    }

    fun removeVoucher() {
        _screenState.update {
            it.copy(
                voucherCode = "",
                voucherStatus = VoucherStatus.IDLE,
                voucherDiscountAmount = 0L,
                voucherMessage = null
            )
        }
    }

    private fun invalidateVoucherAfterCartChange() {
        _screenState.update { state ->
            val voucherNeedsRefresh =
                state.voucherStatus != VoucherStatus.IDLE &&
                        state.voucherStatus != VoucherStatus.INPUT

            if (voucherNeedsRefresh) {
                state.copy(
                    voucherStatus = if (state.voucherCode.isBlank()) {
                        VoucherStatus.IDLE
                    } else {
                        VoucherStatus.INPUT
                    },
                    voucherDiscountAmount = 0L,
                    voucherMessage =
                        "Pesanan berubah. Validasi kembali voucher sebelum checkout."
                )
            } else {
                state
            }
        }
    }

    // =====================================================
    // CHECKOUT
    // =====================================================

    fun processCheckout(
        simulateTechnicalFailure: Boolean = false,
        onResult: (CheckoutResult) -> Unit
    ) {
        val current = uiState.value

        if (
            current.checkoutProcessStatus == CheckoutProcessStatus.PROCESSING ||
            current.checkoutProcessStatus == CheckoutProcessStatus.SUCCESS
        ) {
            return
        }

        if (current.selectedItems.isEmpty()) {
            onResult(CheckoutResult.FAILED)
            return
        }

        if (current.isLoading || current.isError) return

        if (current.voucherStatus == VoucherStatus.VALIDATING) return

        if (!current.isWatuPayBalanceSufficient) {
            onResult(CheckoutResult.INSUFFICIENT_BALANCE)
            return
        }

        _screenState.update {
            it.copy(
                checkoutProcessStatus = CheckoutProcessStatus.PROCESSING,
                checkoutFailureMessage = null,
                checkoutAttemptAt = null
            )
        }

        viewModelScope.launch {
            delay(600L)

            val latest = uiState.value

            if (!latest.isWatuPayBalanceSufficient) {
                _screenState.update {
                    it.copy(
                        checkoutProcessStatus = CheckoutProcessStatus.IDLE
                    )
                }

                onResult(CheckoutResult.INSUFFICIENT_BALANCE)
                return@launch
            }

            val formatter = SimpleDateFormat(
                "d MMM yyyy • HH:mm 'WIB'",
                Locale("id", "ID")
            )
            formatter.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            val attemptTime = formatter.format(Date())

            if (simulateTechnicalFailure) {
                _screenState.update {
                    it.copy(
                        checkoutProcessStatus = CheckoutProcessStatus.FAILED,
                        checkoutFailureMessage =
                            "Terjadi kendala teknis saat memproses pesanan. Silakan coba kembali.",
                        checkoutAttemptAt = attemptTime
                    )
                }
                onResult(CheckoutResult.FAILED)
            } else {
                val directItem = if (latest.detailMode == CartDetailMode.DIRECT_ORDER) latest.directOrderDraft else null

                // Buat snapshot CheckoutSummary sebelum cart dikosongkan
                val summaryItems = latest.selectedItems.map { it.copy() }
                val summarySubtotal = latest.subtotal
                val summaryDiscount = latest.voucherDiscountAmount
                val summaryTotal = latest.checkoutTotal
                val summaryVoucherCode = latest.voucherCode
                val summaryVoucherLabel = latest.voucherLabel
                val summaryWatuPayBalance = (latest.watuPayBalance - summaryTotal).coerceAtLeast(0L)

                val orderIdStr = when {
                    summaryDiscount > 0L -> "GS-ORD-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-003"
                    summaryItems.size > 1 -> "GS-ORD-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-002"
                    else -> "GS-ORD-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-001"
                }

                val checkoutSummary = CheckoutSummary(
                    orderId = orderIdStr,
                    items = summaryItems,
                    subtotal = summarySubtotal,
                    voucherDiscountAmount = summaryDiscount,
                    voucherCode = summaryVoucherCode,
                    voucherLabel = summaryVoucherLabel,
                    checkoutTotal = summaryTotal,
                    watuPayBalance = summaryWatuPayBalance,
                    checkoutAttemptAt = attemptTime
                )

                val checkoutRes = repository.checkout(
                    selectedItems = latest.selectedItems,
                    voucherCode = latest.voucherCode.takeIf { latest.voucherStatus == VoucherStatus.APPLIED },
                    discountAmount = latest.voucherDiscountAmount,
                    totalPayable = latest.checkoutTotal,
                    directServiceItem = directItem
                )

                if (checkoutRes.isSuccess) {
                    latest.voucherClaimId
                        ?.takeIf { latest.voucherStatus == VoucherStatus.APPLIED }
                        ?.let { PromoModule.repository.markClaimUsed(it) }

                    FakeTrackingRepository.addOrdersFromCart(latest.selectedItems)

                    _screenState.update {
                        it.copy(
                            checkoutProcessStatus = CheckoutProcessStatus.SUCCESS,
                            checkoutFailureMessage = null,
                            checkoutAttemptAt = attemptTime,
                            directOrderDraft = null,
                            lastCheckoutSummary = checkoutSummary
                        )
                    }

                    onResult(CheckoutResult.SUCCESS)
                } else {
                    val errorMsg = checkoutRes.exceptionOrNull()?.message
                        ?: "Gagal memproses pesanan. Silakan coba kembali."

                    _screenState.update {
                        it.copy(
                            checkoutProcessStatus = CheckoutProcessStatus.FAILED,
                            checkoutFailureMessage = errorMsg,
                            checkoutAttemptAt = attemptTime
                        )
                    }

                    onResult(CheckoutResult.FAILED)
                }
            }
        }
    }

    fun resetCheckoutProcess() {
        _screenState.update { state ->
            if (
                state.checkoutProcessStatus == CheckoutProcessStatus.PROCESSING ||
                state.checkoutProcessStatus == CheckoutProcessStatus.SUCCESS
            ) {
                state
            } else {
                state.copy(
                    checkoutProcessStatus = CheckoutProcessStatus.IDLE,
                    checkoutFailureMessage = null,
                    checkoutAttemptAt = null
                )
            }
        }
    }

    /** "Pesan Sekarang" dari Katalog: Mode DIRECT_ORDER */
    fun buyNow(service: Service) {
        prepareDirectOrder(service)
    }

    fun finishCheckout() {
        repository.removeSelected()
        resetCheckoutState()
        viewModelScope.launch {
            AuthRepository.refreshProfile()
        }
    }

    private fun resetCheckoutState() {
        _screenState.update {
            it.copy(
                checkoutProcessStatus = CheckoutProcessStatus.IDLE,
                checkoutFailureMessage = null,
                checkoutAttemptAt = null,
                voucherCode = "",
                voucherStatus = VoucherStatus.IDLE,
                voucherDiscountAmount = 0L,
                voucherMessage = null,
                voucherClaimId = null,
                voucherLabel = "",
                lastCheckoutSummary = null
            )
        }
    }
}
