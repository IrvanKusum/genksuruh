package com.example.genksuruh.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import com.example.genksuruh.core.model.Service
import com.example.genksuruh.feature.promo.DiscountType
import com.example.genksuruh.feature.promo.PromoModule
import com.example.genksuruh.feature.promo.effectiveStatus
import com.example.genksuruh.feature.promo.VoucherStatus as ClaimStatus
import com.example.genksuruh.feature.tracking.FakeTrackingRepository

/**
 * Hasil proses checkout.
 */
enum class CheckoutResult {
    SUCCESS,
    FAILED,
    INSUFFICIENT_BALANCE
}

/**
 * ViewModel fitur Keranjang dan Checkout.
 *
 * Tanggung jawab:
 * - Mengelola state UI.
 * - Menerima event dari View.
 * - Memanggil Repository untuk mengelola data.
 * - Mengelola validasi voucher dan checkout.
 *
 * Tidak mengakses CartDummyData secara langsung.
 */
class CartViewModel(
    private val repository: CartRepository = CartRepository.shared
) : ViewModel() {

    // =====================================================
    // UI STATE
    // =====================================================

    /**
     * State internal yang dikelola ViewModel.
     * Tidak menyimpan daftar item karena daftar item dimiliki Repository.
     */
    private val _screenState = MutableStateFlow(CartUiState())

    /**
     * UI State final merupakan gabungan:
     * 1. Data item dari Repository.
     * 2. Status UI dari ViewModel.
     *
     * UI hanya membaca StateFlow ini.
     */
    val uiState: StateFlow<CartUiState> = combine(
        repository.cartItems,
        _screenState
    ) { cartItems, screenState ->

        screenState.copy(
            items = cartItems
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CartUiState()
    )

    init {
        loadCart()
    }

    // =====================================================
    // CART
    // =====================================================

    /**
     * Memuat data keranjang melalui Repository.
     */
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

    /**
     * Hanya untuk pengujian Error State.
     */
    fun simulateLoadError() {
        repository.simulateNextLoadFailure()
        loadCart()
    }

    /**
     * Mengubah pilihan satu item.
     */
    fun toggleItemSelection(itemId: String) {
        repository.toggleItemSelection(itemId)
        invalidateVoucherAfterCartChange()
    }

    /**
     * Memilih semua item atau membatalkan semua pilihan.
     */
    fun selectAllItems() {
        repository.selectAllItems()
        invalidateVoucherAfterCartChange()
    }

    /**
     * Menambah quantity item.
     */
    fun increaseQuantity(itemId: String) {
        repository.increaseQuantity(itemId)
        invalidateVoucherAfterCartChange()
    }

    /**
     * Mengurangi quantity item.
     */
    fun decreaseQuantity(itemId: String) {
        repository.decreaseQuantity(itemId)
        invalidateVoucherAfterCartChange()
    }

    // =====================================================
    // EDIT CART ITEM
    // =====================================================

    /**
     * Mengambil item dari Repository untuk diedit.
     */
    fun startEditingItem(itemId: String) {
        val item = repository.getItemById(itemId) ?: return

        _screenState.update {
            it.copy(
                editingItem = item.copy()
            )
        }
    }

    fun updateEditingLocation(location: String) {
        updateEditingItem { item ->
            item.copy(location = location)
        }
    }

    fun updateEditingDate(date: String) {
        updateEditingItem { item ->
            item.copy(date = date)
        }
    }

    fun updateEditingTime(time: String) {
        updateEditingItem { item ->
            item.copy(time = time)
        }
    }

    fun updateEditingNote(note: String) {
        updateEditingItem { item ->
            item.copy(note = note)
        }
    }

    fun updateEditingQuantity(quantity: Int) {
        updateEditingItem { item ->
            item.copy(
                quantity = quantity.coerceAtLeast(
                    MIN_CART_ITEM_QUANTITY
                )
            )
        }
    }

    fun increaseEditingQuantity() {
        val quantity = _screenState.value.editingItem?.quantity
            ?: return

        updateEditingQuantity(quantity + 1)
    }

    fun decreaseEditingQuantity() {
        val quantity = _screenState.value.editingItem?.quantity
            ?: return

        updateEditingQuantity(quantity - 1)
    }

    /**
     * Helper untuk memperbarui draft item yang sedang diedit.
     */
    private fun updateEditingItem(
        transform: (CartItem) -> CartItem
    ) {
        _screenState.update { state ->

            val item = state.editingItem
                ?: return@update state

            state.copy(
                editingItem = transform(item)
            )
        }
    }

    /**
     * Menyimpan hasil edit melalui Repository.
     */
    fun saveEditingItem() {
        val editedItem = _screenState.value.editingItem ?: return

        val isSaved = repository.updateItem(editedItem)

        _screenState.update {
            it.copy(editingItem = null)
        }

        if (isSaved) {
            invalidateVoucherAfterCartChange()
        }
    }

    /**
     * Membatalkan proses edit.
     */
    fun cancelEditingItem() {
        _screenState.update {
            it.copy(editingItem = null)
        }
    }

    // =====================================================
    // DELETE + UNDO
    // =====================================================

    /**
     * Membuka dialog konfirmasi hapus.
     */
    fun removeItem(itemId: String) {
        val item = repository.getItemById(itemId) ?: return

        _screenState.update {
            it.copy(
                itemPendingDelete = item,
                showDeleteDialog = true
            )
        }
    }

    /**
     * Menutup dialog tanpa menghapus item.
     */
    fun dismissDeleteDialog() {
        _screenState.update {
            it.copy(
                itemPendingDelete = null,
                showDeleteDialog = false
            )
        }
    }

    /**
     * Menghapus item melalui Repository.
     */
    fun confirmRemoveItem() {
        val target = _screenState.value.itemPendingDelete
            ?: return

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

    /**
     * Mengembalikan item yang baru dihapus.
     */
    fun undoRemoveItem() {
        val current = _screenState.value

        val removedItem = current.lastRemovedItem
            ?: return

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

    /**
     * Menutup Snackbar Undo.
     */
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

    /**
     * Memperbarui kode voucher.
     */
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

    /**
     * Memvalidasi voucher membaca promo asli milik PromoModule.
     */
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
            delay(900L)

            val latest = uiState.value
            if (
                latest.voucherCode.trim().uppercase() != code ||
                latest.voucherStatus != VoucherStatus.VALIDATING
            ) return@launch

            val promoRepo = PromoModule.repository
            val userName = PromoModule.session.currentUser

            val promo = promoRepo.promos.first()
                .find { it.code.equals(code, ignoreCase = true) }

            val claim = promo?.let { p ->
                promoRepo.claims.first().find {
                    it.promoId == p.id &&
                            it.userName == userName &&
                            it.effectiveStatus(p) == ClaimStatus.AKTIF
                }
            }

            // Hanya item yang cocok dengan jasa/kategori promo yang dihitung.
            val eligible = promo?.let { p ->
                latest.selectedItems.filter {
                    it.serviceName.equals(p.serviceName, ignoreCase = true) ||
                            it.category.equals(p.category, ignoreCase = true)
                }
            }.orEmpty()
            val base = eligible.sumOf { it.lineTotal }

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
                    fail(VoucherStatus.NOT_FOUND, "Kode voucher tidak ditemukan.")

                promo.isExpired ->
                    fail(VoucherStatus.NOT_FOUND, "Promo ini sudah berakhir.")

                claim == null ->
                    fail(
                        VoucherStatus.NOT_FOUND,
                        "Voucher belum kamu klaim atau sudah terpakai. Klaim dulu di menu Promo."
                    )

                eligible.isEmpty() ->
                    fail(
                        VoucherStatus.NOT_FOUND,
                        "Voucher ini hanya berlaku untuk \"${promo.serviceName}\"."
                    )

                base < promo.minOrder ->
                    fail(
                        VoucherStatus.MINIMUM_NOT_MET,
                        "Minimal transaksi ${formatRupiah(promo.minOrder.toLong())}. " +
                                "Tambahkan jasa atau quantity agar voucher dapat digunakan.",
                        promo.minOrder.toLong()
                    )

                else -> {
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
                            voucherMessage = "Voucher berhasil diterapkan."
                        )
                    }
                }
            }
        }
    }

    /**
     * Menghapus voucher.
     */
    fun removeVoucher() {
        _screenState.update {
            it.copy(
                voucherCode = "",
                voucherStatus = VoucherStatus.IDLE,
                voucherDiscountAmount = 0L,
                voucherClaimId = null,
                voucherLabel = "",
                voucherMessage = null
            )
        }
    }

    /**
     * Membatalkan validasi voucher ketika data keranjang berubah.
     */
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

    /**
     * Memproses checkout menggunakan simulasi WatuPay.
     *
     * Repository menyediakan data keranjang.
     * ViewModel melakukan validasi dan mengelola hasil transaksi.
     */
    fun processCheckout(
        simulateTechnicalFailure: Boolean = false,
        onResult: (CheckoutResult) -> Unit
    ) {
        val current = uiState.value

        // Mencegah checkout ganda.
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

        // Validasi kecukupan saldo.
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
            delay(1200L)

            val latest = uiState.value

            // Validasi ulang saldo.
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

            formatter.timeZone =
                TimeZone.getTimeZone("Asia/Jakarta")

            val attemptTime = formatter.format(Date())

            if (simulateTechnicalFailure) {

                // Gagal teknis:
                // saldo, item, dan voucher tidak berubah.
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

                // Berhasil:
                // Tandai voucher terpakai, catat pesanan ke Riwayat Pesanan, & potong saldo sekali.
                latest.voucherClaimId
                    ?.takeIf { latest.voucherStatus == VoucherStatus.APPLIED }
                    ?.let { PromoModule.repository.markClaimUsed(it) }

                FakeTrackingRepository.addOrdersFromCart(latest.selectedItems)

                val payable = latest.checkoutTotal

                _screenState.update {
                    it.copy(
                        watuPayBalance = (
                                it.watuPayBalance - payable
                                ).coerceAtLeast(0L),
                        checkoutProcessStatus = CheckoutProcessStatus.SUCCESS,
                        checkoutFailureMessage = null,
                        checkoutAttemptAt = attemptTime
                    )
                }

                // Item tetap disimpan untuk halaman Success.
                onResult(CheckoutResult.SUCCESS)
            }
        }
    }

    /**
     * Mengatur ulang status setelah kegagalan checkout.
     */
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

    /** "Pesan Sekarang": masukkan jasa ke keranjang, centang hanya itu, lalu checkout. */
    fun buyNow(service: Service) {
        repository.addService(service)
        repository.selectOnly(service.id)
        resetCheckoutState()
    }

    /** Dipanggil saat meninggalkan layar Sukses: bersihkan item yang sudah dibayar. */
    fun finishCheckout() {
        repository.removeSelected()
        resetCheckoutState()
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
                voucherLabel = ""
            )
        }
    }
}