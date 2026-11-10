package com.example.genksuruh.feature.cart

/**
 * Data khusus untuk satu entri di Keranjang.
 *
 * Catatan: pada saat implementasi ini dibuat, model katalog bersama
 * `core/model/Service.kt` (tanggung jawab Irvan) belum tersedia di repository.
 * Agar tidak menduplikasi atau mendahului desain model bersama tersebut,
 * [CartItem] untuk sementara berdiri sendiri (memuat info layanan + info
 * pemesanan sekaligus). Setelah `Service` tersedia dan disepakati bentuknya,
 * [CartItem] dapat direfactor agar membungkus `Service` ditambah field
 * khusus keranjang (lokasi, tanggal, waktu, catatan, quantity, selected).
 */
data class CartItem(
    val id: String,
    val serviceName: String,
    val category: String,
    val providerName: String,
    val price: Long,
    val imageUrl: String? = null,
    val location: String,
    val date: String,
    val time: String,
    val note: String = "",
    val quantity: Int = 1,
    val isSelected: Boolean = true
) {
    val lineTotal: Long
        get() = price * quantity
}

/** Jumlah quantity minimum yang diperbolehkan untuk satu item keranjang. */
const val MIN_CART_ITEM_QUANTITY = 1

/**
 * Memformat nominal Rupiah tanpa desimal menggunakan pemisah ribuan titik,
 * misalnya 8000 -> "Rp8.000", 75000 -> "Rp75.000".
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
