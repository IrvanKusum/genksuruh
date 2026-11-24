package com.example.genksuruh.feature.cart

import com.example.genksuruh.core.model.Service
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
 * Kontrak Repository Keranjang.
 */
interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    suspend fun loadCart()
    fun simulateNextLoadFailure()
    fun getItemById(itemId: String): CartItem?
    fun toggleItemSelection(itemId: String)
    fun selectAllItems()
    fun increaseQuantity(itemId: String)
    fun decreaseQuantity(itemId: String)
    fun updateItem(updatedItem: CartItem): Boolean
    fun addCartItemWithDetails(item: CartItem): Boolean
    fun removeItem(itemId: String): RemovedCartItem?
    fun restoreItem(item: CartItem, index: Int)
    fun addService(service: Service): Boolean
    fun isInCart(serviceId: String): Boolean
    fun selectOnly(itemId: String)
    fun removeSelected()
    suspend fun checkout(
        selectedItems: List<CartItem>,
        voucherCode: String?,
        discountAmount: Long,
        totalPayable: Long,
        directServiceItem: CartItem? = null
    ): Result<Unit>

    companion object : CartRepository by SupabaseCartRepository {
        val shared: CartRepository = SupabaseCartRepository
    }
}

/**
 * Implementasi Dummy / In-Memory CartRepository.
 */
object DummyCartRepository : CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private var hasLoaded = false
    private var shouldFailNextLoad = false

    override suspend fun loadCart() {
        delay(600L)

        if (shouldFailNextLoad) {
            shouldFailNextLoad = false
            throw IllegalStateException(
                CartDummyData.GENERIC_ERROR_MESSAGE
            )
        }

        if (!hasLoaded) {
            val existing = _cartItems.value
            val dummy = CartDummyData.defaultItems()
                .map { it.copy(isSelected = false) }
                .filter { d -> existing.none { it.id == d.id } }

            _cartItems.value = existing + dummy
            hasLoaded = true
        }
    }

    override fun simulateNextLoadFailure() {
        shouldFailNextLoad = true
    }

    override fun getItemById(itemId: String): CartItem? {
        return _cartItems.value.find { it.id == itemId }
    }

    override fun toggleItemSelection(itemId: String) {
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

    override fun selectAllItems() {
        _cartItems.update { items ->
            val nextSelected =
                items.isNotEmpty() && !items.all { it.isSelected }

            items.map { item ->
                item.copy(isSelected = nextSelected)
            }
        }
    }

    override fun increaseQuantity(itemId: String) {}

    override fun decreaseQuantity(itemId: String) {}

    override fun updateItem(updatedItem: CartItem): Boolean {
        val currentItems = _cartItems.value

        if (currentItems.none { it.id == updatedItem.id }) {
            return addCartItemWithDetails(updatedItem)
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

    override fun addCartItemWithDetails(item: CartItem): Boolean {
        _cartItems.update { it + item.copy(quantity = 1, isSelected = true) }
        return true
    }

    override fun removeItem(itemId: String): RemovedCartItem? {
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

    override fun restoreItem(item: CartItem, index: Int) {
        val currentItems = _cartItems.value

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

    override fun addService(service: Service): Boolean {
        if (_cartItems.value.any { it.id == service.id }) return false

        val item = CartItem(
            id = service.id,
            serviceId = service.id,
            serviceName = service.title,
            category = service.category,
            providerName = service.providerName,
            price = service.price,
            imageUrl = service.imageUrl,
            location = "Area Kampus UB",
            date = try { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale("id", "ID"))) } catch (_: Exception) { "10 November 2026" },
            time = "12.00 WIB",
            note = "",
            quantity = 1,
            isSelected = false
        )
        _cartItems.update { it + item }
        return true
    }

    override fun isInCart(serviceId: String): Boolean =
        _cartItems.value.any { it.id == serviceId }

    override fun selectOnly(itemId: String) {
        _cartItems.update { items ->
            items.map { it.copy(isSelected = it.id == itemId) }
        }
    }

    override fun removeSelected() {
        _cartItems.update { items -> items.filterNot { it.isSelected } }
    }

    override suspend fun checkout(
        selectedItems: List<CartItem>,
        voucherCode: String?,
        discountAmount: Long,
        totalPayable: Long,
        directServiceItem: CartItem?
    ): Result<Unit> {
        removeSelected()
        return Result.success(Unit)
    }
}
