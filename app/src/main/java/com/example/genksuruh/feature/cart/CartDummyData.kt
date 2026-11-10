
package com.example.genksuruh.feature.cart

/**
 * Sumber tunggal untuk dummy data Keranjang.
 */
object CartDummyData {

    fun defaultItems(): List<CartItem> = listOf(
        CartItem(
            id = "svc-1",
            serviceName = "Antar Makanan Kantin FILKOM",
            category = "Antar Makanan",
            providerName = "Dimas Pratama",
            price = 8_000,
            location = "Gedung FILKOM Lt. 2",
            date = "1 November 2026",
            time = "12.30 WIB",
            quantity = 1,
            isSelected = false
        ),
        CartItem(
            id = "svc-2",
            serviceName = "Titip Beli Jajan Indomaret",
            category = "Beli Jajan",
            providerName = "Nadia Putri",
            price = 5_000,
            location = "Asrama Putra Blok B",
            date = "1 November 2026",
            time = "13.00 WIB",
            quantity = 1,
            isSelected = false
        )
    )

    /** Preview: hanya item pertama dipilih. */
    fun partialSelectionItems(): List<CartItem> =
        defaultItems().mapIndexed { index, item ->
            item.copy(isSelected = index == 0)
        }

    /** Preview: tidak ada item dipilih. */
    fun noSelectionItems(): List<CartItem> =
        defaultItems().map { it.copy(isSelected = false) }

    const val GENERIC_ERROR_MESSAGE =
        "Terjadi kendala saat mengambil data keranjang. Periksa koneksi internet dan coba kembali."
}