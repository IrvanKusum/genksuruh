package com.example.genksuruh.feature.cart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data khusus untuk satu entri di Keranjang.
 */
@Serializable
data class CartItem(
    val id: String,
    @SerialName("service_id")
    val serviceId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("service_name")
    val serviceName: String = "",
    val category: String = "",
    @SerialName("provider_name")
    val providerName: String = "",
    val price: Long = 0L,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val note: String = "",
    val quantity: Int = 1,
    @SerialName("is_selected")
    val isSelected: Boolean = true
) {
    val lineTotal: Long
        get() = price * quantity
}

/** Jumlah quantity minimum yang diperbolehkan untuk satu item keranjang. */
const val MIN_CART_ITEM_QUANTITY = 1

/**
 * Memformat nominal Rupiah tanpa desimal menggunakan pemisah ribuan titik.
 */
fun formatRupiah(amount: Long): String {
    val safeAmount = amount.coerceAtLeast(0)
    val grouped = safeAmount.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return "Rp$grouped"
}
