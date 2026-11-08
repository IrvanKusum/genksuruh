
package com.example.genksuruh.feature.cart

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data item yang dihapus beserta posisi sebelumnya.
 * Digunakan untuk fitur Undo.
 */
data class RemovedCartItem(
    val item: CartItem,
    val index: Int
)

/**
 * Repository sebagai satu-satunya sumber data Keranjang.
 *
 * Saat ini menggunakan dummy data.
 * Nantinya sumber data bisa diganti Supabase tanpa mengubah ViewModel
 * atau Composable.
 */
class CartRepository {

    private val _cartItems =
        MutableStateFlow<List<CartItem>>(emptyList())

    val cartItems: StateFlow<List<CartItem>> =
        _cartItems.asStateFlow()

    private var hasLoaded = false
    private var shouldFailNextLoad = false

    /**
     * Mengambil data keranjang.
     *
     * Simulasi delay menyerupai pengambilan data server.
     */
    suspend fun loadCart() {
        delay(600L)

        if (shouldFailNextLoad) {
            shouldFailNextLoad = false
            throw IllegalStateException(
                CartDummyData.GENERIC_ERROR_MESSAGE
            )
        }

        // Data hanya dimuat pertama kali.
        // Jika seluruh item dihapus, tidak akan muncul kembali
        // saat pengguna membuka ulang keranjang.
        if (!hasLoaded) {
            _cartItems.value = CartDummyData.defaultItems()
                .map { item ->
                    item.copy(isSelected = false)
                }

            hasLoaded = true
        }
    }

    /**
     * Simulasi kegagalan pengambilan data untuk pengujian.
     */
    fun simulateNextLoadFailure() {
        shouldFailNextLoad = true
    }

    /**
     * Mengambil satu item berdasarkan ID.
     */
    fun getItemById(itemId: String): CartItem? {
        return _cartItems.value.find { it.id == itemId }
    }

    /**
     * Mengubah pilihan satu item.
     */
    fun toggleItemSelection(itemId: String) {
        _cartItems.update { items ->
            items.map { item ->
                if (item.id == itemId) {
                    item.copy(isSelected = !item.isSelected)
                } else {
                    item
                }
            }
        }
    }

    /**
     * Memilih semua item atau membatalkan seluruh pilihan.
     */
    fun selectAllItems() {
        _cartItems.update { items ->
            val nextSelected =
                items.isNotEmpty() && !items.all { it.isSelected }

            items.map { item ->
                item.copy(isSelected = nextSelected)
            }
        }
    }

    /**
     * Menambah quantity item.
     */
    fun increaseQuantity(itemId: String) {
        _cartItems.update { items ->
            items.map { item ->
                if (item.id == itemId) {
                    item.copy(quantity = item.quantity + 1)
                } else {
                    item
                }
            }
        }
    }

    /**
     * Mengurangi quantity item dengan batas minimum.
     */
    fun decreaseQuantity(itemId: String) {
        _cartItems.update { items ->
            items.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        quantity = (
                                item.quantity - 1
                                ).coerceAtLeast(MIN_CART_ITEM_QUANTITY)
                    )
                } else {
                    item
                }
            }
        }
    }

    /**
     * Memperbarui data item setelah proses edit.
     * Status selected tetap mengikuti data sebelumnya.
     */
    fun updateItem(updatedItem: CartItem): Boolean {
        val currentItems = _cartItems.value

        if (currentItems.none { it.id == updatedItem.id }) {
            return false
        }

        _cartItems.value = currentItems.map { current ->
            if (current.id == updatedItem.id) {
                updatedItem.copy(
                    isSelected = current.isSelected
                )
            } else {
                current
            }
        }

        return true
    }

    /**
     * Menghapus item dan menyimpan posisi sebelumnya.
     */
    fun removeItem(itemId: String): RemovedCartItem? {
        val currentItems = _cartItems.value

        val index = currentItems.indexOfFirst {
            it.id == itemId
        }

        if (index == -1) return null

        val removedItem = currentItems[index]

        _cartItems.value = currentItems.filterNot {
            it.id == itemId
        }

        return RemovedCartItem(
            item = removedItem,
            index = index
        )
    }

    /**
     * Mengembalikan item yang sebelumnya dihapus.
     */
    fun restoreItem(
        item: CartItem,
        index: Int
    ) {
        val currentItems = _cartItems.value

        // Hindari item duplikat.
        if (currentItems.any { it.id == item.id }) return

        val restoreIndex = index.coerceIn(
            0,
            currentItems.size
        )

        val restoredItems = currentItems.toMutableList().apply {
            add(restoreIndex, item)
        }

        _cartItems.value = restoredItems
    }
}