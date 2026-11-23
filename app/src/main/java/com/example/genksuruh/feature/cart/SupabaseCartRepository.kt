package com.example.genksuruh.feature.cart

import android.util.Log
import com.example.genksuruh.core.auth.AuthRepository
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.network.SupabaseClient
import com.example.genksuruh.feature.catalog.SupabaseCatalogRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "SupabaseCartRepo"

private fun JsonObject.str(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { k -> this[k]?.jsonPrimitive?.contentOrNull }

private fun JsonObject.long(key: String): Long? =
    this[key]?.jsonPrimitive?.contentOrNull?.toLongOrNull()

private fun JsonObject.int(key: String): Int? =
    this[key]?.jsonPrimitive?.contentOrNull?.toIntOrNull()

private fun JsonObject.bool(key: String): Boolean? =
    this[key]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()

private fun toPostgresDate(dateStr: String): String {
    if (dateStr.isBlank()) return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    if (dateStr.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) return dateStr
    val locale = Locale("id", "ID")
    val patterns = listOf("d MMMM yyyy", "d MMM yyyy", "dd MMMM yyyy", "dd MMM yyyy", "yyyy-MM-dd")
    for (p in patterns) {
        runCatching {
            val parsed = SimpleDateFormat(p, locale).parse(dateStr)
            if (parsed != null) {
                return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed)
            }
        }
    }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

private fun getTodayFormattedDate(): String {
    val locale = Locale("id", "ID")
    return try {
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
    } catch (_: Exception) {
        SimpleDateFormat("d MMMM yyyy", locale).format(Date())
    }
}

private fun toPostgresTime(timeStr: String): String {
    if (timeStr.isBlank()) return "09:00:00"
    val match = Regex("""(\d{1,2})[.:](\d{2})(?:[.:](\d{2}))?""").find(timeStr)
    if (match != null) {
        val h = match.groupValues[1].padStart(2, '0')
        val m = match.groupValues[2].padStart(2, '0')
        val s = match.groupValues.getOrNull(3)?.ifBlank { "00" } ?: "00"
        return "$h:$m:$s"
    }
    return "09:00:00"
}

/**
 * Repository Keranjang berbasis Supabase Postgrest & Auth.
 */
object SupabaseCartRepository : CartRepository {

    private val db get() = SupabaseClient.client.postgrest

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private var shouldFailNextLoad = false

    private fun myUserId(): String? {
        val accountId = AuthRepository.account.value?.id
        val authUserId = runCatching { SupabaseClient.client.auth.currentUserOrNull()?.id }.getOrNull()
            ?: runCatching { SupabaseClient.client.auth.currentSessionOrNull()?.user?.id }.getOrNull()
        val uid = accountId ?: authUserId
        Log.d(TAG, "myUserId resolved: accountId=$accountId, authUserId=$authUserId -> uid=$uid")
        return uid
    }

    override suspend fun loadCart() {
        val uid = myUserId()
        if (uid == null) {
            Log.w(TAG, "loadCart: User belum login, keranjang dibersihkan.")
            _cartItems.value = emptyList()
            return
        }

        if (shouldFailNextLoad) {
            shouldFailNextLoad = false
            throw IllegalStateException(CartDummyData.GENERIC_ERROR_MESSAGE)
        }

        try {
            val catalogServices = runCatching {
                SupabaseCatalogRepository.services.value.ifEmpty {
                    SupabaseCatalogRepository.loadServices()
                    SupabaseCatalogRepository.services.value
                }
            }.getOrDefault(emptyList())

            val serviceMap = catalogServices.associateBy { it.id }

            val rows = db.from("cart_items").select {
                filter {
                    eq("user_id", uid)
                }
            }.decodeList<JsonObject>()

            Log.d(TAG, "loadCart: Berhasil membaca ${rows.size} baris dari tabel cart_items di Supabase.")

            val list = rows.mapNotNull { row ->
                val id = row.str("id") ?: return@mapNotNull null
                val serviceId = row.str("service_id") ?: ""
                val service = serviceMap[serviceId]

                CartItem(
                    id = id,
                    serviceId = serviceId,
                    userId = uid,
                    serviceName = service?.title ?: row.str("service_name") ?: "Jasa Kampus",
                    category = service?.category ?: row.str("category") ?: "Umum",
                    providerName = service?.providerName ?: row.str("provider_name") ?: "Runner Kampus",
                    price = service?.price ?: row.long("price") ?: 5000L,
                    imageUrl = service?.imageUrl ?: row.str("image_url"),
                    location = row.str("location") ?: "Area Kampus UB",
                    date = row.str("booking_date", "date") ?: getTodayFormattedDate(),
                    time = row.str("booking_time", "time") ?: "12.00 WIB",
                    note = row.str("note") ?: "",
                    quantity = 1,
                    isSelected = row.bool("is_selected") ?: true
                )
            }

            _cartItems.value = list
        } catch (e: Exception) {
            Log.e(TAG, "loadCart gagal memuat data dari Supabase: ${e.message}", e)
        }
    }

    override fun simulateNextLoadFailure() {
        shouldFailNextLoad = true
    }

    override fun getItemById(itemId: String): CartItem? =
        _cartItems.value.find { it.id == itemId || it.serviceId == itemId }

    override fun toggleItemSelection(itemId: String) {
        val uid = myUserId()
        _cartItems.update { items ->
            items.map { item ->
                if (item.id == itemId || item.serviceId == itemId) {
                    val newSel = !item.isSelected
                    if (uid != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            runCatching {
                                db.from("cart_items").update(buildJsonObject { put("is_selected", newSel) }) {
                                    filter {
                                        eq("user_id", uid)
                                        eq("id", item.id)
                                    }
                                }
                            }.onFailure { e -> Log.e(TAG, "toggleItemSelection Supabase update gagal: ${e.message}", e) }
                        }
                    }
                    item.copy(isSelected = newSel)
                } else {
                    item
                }
            }
        }
    }

    override fun selectAllItems() {
        val uid = myUserId()
        val nextSelected = _cartItems.value.isNotEmpty() && !_cartItems.value.all { it.isSelected }

        _cartItems.update { items ->
            items.map { item ->
                if (uid != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            db.from("cart_items").update(buildJsonObject { put("is_selected", nextSelected) }) {
                                filter {
                                    eq("user_id", uid)
                                    eq("id", item.id)
                                }
                            }
                        }.onFailure { e -> Log.e(TAG, "selectAllItems Supabase update gagal: ${e.message}", e) }
                    }
                }
                item.copy(isSelected = nextSelected)
            }
        }
    }

    override fun increaseQuantity(itemId: String) {}

    override fun decreaseQuantity(itemId: String) {}

    override fun addCartItemWithDetails(item: CartItem): Boolean {
        val uid = myUserId() ?: "anon"
        val newItem = item.copy(
            id = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id,
            userId = uid,
            quantity = 1,
            isSelected = true
        )

        _cartItems.update { current -> current + newItem }

        if (uid != "anon") {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    Log.d(TAG, "INSERTing new cart_item to Supabase: id=${newItem.id}, service_id=${newItem.serviceId}")
                    val body = buildJsonObject {
                        put("id", newItem.id)
                        put("user_id", uid)
                        put("service_id", newItem.serviceId.ifBlank { newItem.id })
                        put("location", newItem.location)
                        put("booking_date", toPostgresDate(newItem.date))
                        put("booking_time", toPostgresTime(newItem.time))
                        put("note", newItem.note)
                        put("quantity", 1)
                        put("is_selected", newItem.isSelected)
                    }
                    db.from("cart_items").insert(body)
                    Log.d(TAG, "Berhasil INSERT ke tabel cart_items di Supabase!")
                }.onFailure { e ->
                    Log.e(TAG, "Gagal INSERT ke cart_items Supabase: ${e.message}", e)
                }
            }
        }
        return true
    }

    override fun updateItem(updatedItem: CartItem): Boolean {
        val currentItems = _cartItems.value
        val targetIndex = currentItems.indexOfFirst { it.id == updatedItem.id || (updatedItem.id.isBlank() && it.serviceId == updatedItem.serviceId) }
        if (targetIndex == -1) {
            return addCartItemWithDetails(updatedItem)
        }

        val targetItem = currentItems[targetIndex]
        val newItem = updatedItem.copy(id = targetItem.id, quantity = 1)

        _cartItems.value = currentItems.map { current ->
            if (current.id == targetItem.id) {
                newItem.copy(isSelected = current.isSelected)
            } else {
                current
            }
        }

        val uid = myUserId()
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    Log.d(TAG, "UPDATEing cart_items in Supabase for item.id=${targetItem.id}")
                    val updateBody = buildJsonObject {
                        put("location", newItem.location)
                        put("booking_date", toPostgresDate(newItem.date))
                        put("booking_time", toPostgresTime(newItem.time))
                        put("note", newItem.note)
                        put("quantity", 1)
                    }
                    db.from("cart_items").update(updateBody) {
                        filter {
                            eq("id", targetItem.id)
                            eq("user_id", uid)
                        }
                    }
                    Log.d(TAG, "Berhasil UPDATE cart_items di Supabase!")
                }.onFailure { e ->
                    Log.e(TAG, "Gagal UPDATE cart_items Supabase: ${e.message}", e)
                }
            }
        }

        return true
    }

    override fun removeItem(itemId: String): RemovedCartItem? {
        val currentItems = _cartItems.value
        val index = currentItems.indexOfFirst { it.id == itemId || it.serviceId == itemId }
        if (index == -1) return null

        val removedItem = currentItems[index]
        _cartItems.value = currentItems.filterNot { it.id == removedItem.id }

        val uid = myUserId()
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    Log.d(TAG, "Deleting item from cart_items in Supabase: id=${removedItem.id}")
                    db.from("cart_items").delete {
                        filter {
                            eq("user_id", uid)
                            eq("id", removedItem.id)
                        }
                    }
                }.onFailure { e -> Log.e(TAG, "removeItem Supabase delete gagal: ${e.message}", e) }
            }
        }

        return RemovedCartItem(item = removedItem, index = index)
    }

    override fun restoreItem(item: CartItem, index: Int) {
        val currentItems = _cartItems.value
        if (currentItems.any { it.id == item.id }) return

        val restoreIndex = index.coerceIn(0, currentItems.size)
        _cartItems.value = currentItems.toMutableList().apply { add(restoreIndex, item) }

        val uid = myUserId()
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    val body = buildJsonObject {
                        put("id", item.id)
                        put("user_id", uid)
                        put("service_id", item.serviceId.ifBlank { item.id })
                        put("location", item.location)
                        put("booking_date", toPostgresDate(item.date))
                        put("booking_time", toPostgresTime(item.time))
                        put("note", item.note)
                        put("quantity", 1)
                        put("is_selected", item.isSelected)
                    }
                    db.from("cart_items").insert(body)
                }.onFailure { e -> Log.e(TAG, "restoreItem Supabase insert gagal: ${e.message}", e) }
            }
        }
    }

    override fun addService(service: Service): Boolean {
        val uid = myUserId() ?: "anon"
        val newItem = CartItem(
            id = UUID.randomUUID().toString(),
            serviceId = service.id,
            userId = uid,
            serviceName = service.title,
            category = service.category,
            providerName = service.providerName,
            price = service.price,
            imageUrl = service.imageUrl,
            location = "Area Kampus UB",
            date = getTodayFormattedDate(),
            time = "12.00 WIB",
            note = "",
            quantity = 1,
            isSelected = true
        )

        _cartItems.update { it + newItem }

        if (uid != "anon") {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    Log.d(TAG, "INSERTing new item to cart_items in Supabase: uid=$uid, service_id=${service.id}")
                    val body = buildJsonObject {
                        put("id", newItem.id)
                        put("user_id", uid)
                        put("service_id", service.id)
                        put("location", newItem.location)
                        put("booking_date", toPostgresDate(newItem.date))
                        put("booking_time", toPostgresTime(newItem.time))
                        put("note", newItem.note)
                        put("quantity", 1)
                        put("is_selected", newItem.isSelected)
                    }
                    db.from("cart_items").insert(body)
                    Log.d(TAG, "Berhasil INSERT ke tabel cart_items di Supabase!")
                }.onFailure { e ->
                    Log.e(TAG, "addService Supabase insert gagal: ${e.message}", e)
                }
            }
        }

        return true
    }

    override fun isInCart(serviceId: String): Boolean =
        _cartItems.value.any { it.serviceId == serviceId || it.id == serviceId }

    override fun selectOnly(itemId: String) {
        val uid = myUserId()
        _cartItems.update { items ->
            items.map { item ->
                val selected = (item.id == itemId || item.serviceId == itemId)
                if (uid != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            db.from("cart_items").update(buildJsonObject { put("is_selected", selected) }) {
                                filter {
                                    eq("user_id", uid)
                                    eq("id", item.id)
                                }
                            }
                        }.onFailure { e -> Log.e(TAG, "selectOnly Supabase update gagal: ${e.message}", e) }
                    }
                }
                item.copy(isSelected = selected)
            }
        }
    }

    override fun removeSelected() {
        val uid = myUserId()
        val selected = _cartItems.value.filter { it.isSelected }

        _cartItems.update { items -> items.filterNot { it.isSelected } }

        if (uid != null && selected.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                selected.forEach { item ->
                    runCatching {
                        db.from("cart_items").delete {
                            filter {
                                eq("user_id", uid)
                                eq("id", item.id)
                            }
                        }
                    }.onFailure { e -> Log.e(TAG, "removeSelected Supabase delete gagal: ${e.message}", e) }
                }
            }
        }
    }

    override suspend fun checkout(
        selectedItems: List<CartItem>,
        voucherCode: String?,
        discountAmount: Long,
        totalPayable: Long,
        directServiceItem: CartItem?
    ): Result<Unit> {
        val uid = myUserId() ?: return Result.failure(IllegalStateException("Silakan login terlebih dahulu"))

        return try {
            val rpcParams = buildJsonObject {
                if (directServiceItem != null) {
                    put("p_cart_item_ids", JsonNull)
                    if (!voucherCode.isNullOrBlank()) {
                        put("p_voucher_code", voucherCode)
                    } else {
                        put("p_voucher_code", JsonNull)
                    }
                    put("p_app_fee", 0L)
                    put("p_direct_service_id", directServiceItem.serviceId.ifBlank { directServiceItem.id })
                    put("p_direct_location", directServiceItem.location)
                    put("p_direct_booking_date", toPostgresDate(directServiceItem.date))
                    put("p_direct_booking_time", toPostgresTime(directServiceItem.time))
                    put("p_direct_note", directServiceItem.note)
                } else {
                    val uuidList = selectedItems.map { JsonPrimitive(it.id) }
                    put("p_cart_item_ids", JsonArray(uuidList))
                    if (!voucherCode.isNullOrBlank()) {
                        put("p_voucher_code", voucherCode)
                    } else {
                        put("p_voucher_code", JsonNull)
                    }
                    put("p_app_fee", 0L)
                    put("p_direct_service_id", JsonNull)
                    put("p_direct_location", JsonNull)
                    put("p_direct_booking_date", JsonNull)
                    put("p_direct_booking_time", JsonNull)
                    put("p_direct_note", JsonNull)
                }
            }

            Log.d(TAG, "Executing RPC process_checkout with params: $rpcParams")
            val rpcRes = runCatching {
                db.rpc("process_checkout", rpcParams)
            }

            if (rpcRes.isFailure) {
                val err = rpcRes.exceptionOrNull()
                Log.e(TAG, "RPC process_checkout error: ${err?.message}", err)
                return Result.failure(err ?: Exception("Gagal memproses checkout via RPC"))
            }

            Log.d(TAG, "RPC process_checkout berhasil!")

            AuthRepository.refreshProfile()

            if (directServiceItem == null) {
                removeSelectedItemsFromDb(uid, selectedItems)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Checkout error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun removeSelectedItemsFromDb(userId: String, items: List<CartItem>) {
        items.forEach { item ->
            runCatching {
                db.from("cart_items").delete {
                    filter {
                        eq("user_id", userId)
                        eq("id", item.id)
                    }
                }
            }
        }
        _cartItems.update { current -> current.filterNot { c -> items.any { it.id == c.id } } }
    }
}
