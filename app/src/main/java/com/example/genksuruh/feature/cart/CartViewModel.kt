
package com.example.genksuruh.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    private val repository: CartRepository = CartRepository()
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
                voucherMessage = null
            )
        }
    }

    /**
     * Memvalidasi voucher menggunakan dummy data.
     */
    fun applyVoucher() {
        val initial = uiState.value

        val code = initial.voucherCode
            .trim()
            .uppercase()

        if (
            code.isBlank() ||
            initial.voucherStatus == VoucherStatus.VALIDATING
        ) {
            return
        }

        _screenState.update {
            it.copy(
                voucherCode = code,
                voucherStatus = VoucherStatus.VALIDATING,
                voucherDiscountAmount = 0L,
                voucherMessage = null
            )
        }

        viewModelScope.launch {
            delay(900L)

            val latest = uiState.value

            // Batalkan hasil validasi jika kode atau
            // kondisi keranjang telah berubah.
            if (
                latest.voucherCode.trim().uppercase() != code ||
                latest.voucherStatus != VoucherStatus.VALIDATING
            ) {
                return@launch
            }

            when {
                code != DUMMY_VOUCHER_CODE -> {
                    _screenState.update {
                        it.copy(
                            voucherStatus = VoucherStatus.NOT_FOUND,
                            voucherDiscountAmount = 0L,
                            voucherMessage =
                                "Kode voucher tidak ditemukan atau tidak dapat digunakan."
                        )
                    }
                }

                latest.subtotal < latest.voucherMinimumTransaction -> {
                    _screenState.update {
                        it.copy(
                            voucherStatus = VoucherStatus.MINIMUM_NOT_MET,
                            voucherDiscountAmount = 0L,
                            voucherMessage =
                                "Minimal transaksi ${formatRupiah(latest.voucherMinimumTransaction)}. Tambahkan jasa atau quantity agar voucher dapat digunakan."
                        )
                    }
                }

                else -> {
                    val discount = (
                            latest.subtotal *
                                    latest.voucherDiscountPercent / 100L
                            ).coerceAtMost(latest.subtotal)

                    _screenState.update {
                        it.copy(
                            voucherStatus = VoucherStatus.APPLIED,
                            voucherDiscountAmount = discount,
                            voucherMessage =
                                "Voucher berhasil diterapkan."
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
                // potong saldo sekali.
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

    // =====================================================
    // CONSTANTS
    // =====================================================

    private companion object {
        const val DUMMY_VOUCHER_CODE = "GENK20"
    }
}