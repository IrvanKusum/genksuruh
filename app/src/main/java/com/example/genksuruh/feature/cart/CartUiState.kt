
package com.example.genksuruh.feature.cart

/** Kondisi validasi voucher. */
enum class VoucherStatus {
    IDLE,
    INPUT,
    VALIDATING,
    APPLIED,
    NOT_FOUND,
    MINIMUM_NOT_MET
}

/** Kondisi proses checkout. */
enum class CheckoutProcessStatus {
    IDLE,
    PROCESSING,
    FAILED,
    SUCCESS
}

/** Representasi tunggal kondisi UI Keranjang dan Checkout. */
data class CartUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val items: List<CartItem> = emptyList(),

    // Dialog hapus item.
    val itemPendingDelete: CartItem? = null,
    val showDeleteDialog: Boolean = false,

    // Item sementara yang sedang diedit.
    val editingItem: CartItem? = null,

    // Undo item yang dihapus.
    val lastRemovedItem: CartItem? = null,
    val lastRemovedItemIndex: Int = -1,
    val showUndoSnackbar: Boolean = false,

    // State voucher.
    val voucherCode: String = "",
    val voucherStatus: VoucherStatus = VoucherStatus.IDLE,
    val voucherDiscountAmount: Long = 0L,
    val voucherDiscountPercent: Int = 20,
    val voucherMinimumTransaction: Long = 30_000L,
    val voucherMessage: String? = null,
    val voucherClaimId: String? = null,
    val voucherLabel: String = "",

    // Saldo WatuPay (dummy).
    val watuPayBalance: Long = 145_500L,

    // Proses checkout.
    val checkoutProcessStatus: CheckoutProcessStatus =
        CheckoutProcessStatus.IDLE,
    val checkoutFailureMessage: String? = null,
    val checkoutAttemptAt: String? = null
) {

    val totalItemCount: Int
        get() = items.size

    val selectedItems: List<CartItem>
        get() = items.filter { it.isSelected }

    val selectedCount: Int
        get() = selectedItems.size

    val isAllSelected: Boolean
        get() = items.isNotEmpty() && items.all { it.isSelected }

    /** Subtotal hanya menghitung item yang dipilih. */
    val subtotal: Long
        get() = selectedItems.sumOf { it.lineTotal }

    /** Total keranjang sebelum diskon voucher. */
    val total: Long
        get() = subtotal

    /** Total checkout setelah diskon voucher. */
    val checkoutTotal: Long
        get() = (subtotal - voucherDiscountAmount).coerceAtLeast(0L)

    /** Pemeriksaan kecukupan saldo WatuPay. */
    val isWatuPayBalanceSufficient: Boolean
        get() = checkoutTotal <= watuPayBalance

    /** Nominal kekurangan saldo. */
    val balanceShortfall: Long
        get() = (checkoutTotal - watuPayBalance).coerceAtLeast(0L)

    val isCheckoutEnabled: Boolean
        get() = !isLoading && !isError && selectedCount > 0

    val isCartEmpty: Boolean
        get() = !isLoading && !isError && items.isEmpty()

    val isVoucherApplied: Boolean
        get() = voucherStatus == VoucherStatus.APPLIED

    val isCheckoutProcessing: Boolean
        get() = checkoutProcessStatus == CheckoutProcessStatus.PROCESSING
}
