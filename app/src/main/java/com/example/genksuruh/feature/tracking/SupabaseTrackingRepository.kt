package com.example.genksuruh.feature.tracking

import android.util.Log
import com.example.genksuruh.core.auth.AuthRepository
import com.example.genksuruh.core.network.SupabaseClient
import com.example.genksuruh.feature.cart.CartItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Serializable
private data class OrderStatusUpdateDto(
    val status: OrderStatus,
    @SerialName("progress_percent")
    val progressPercent: Int? = null,
)

object SupabaseTrackingRepository : TrackingRepository {

    private val client = SupabaseClient.client
    private val refreshTrigger = MutableStateFlow(0L)

    private fun currentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
            ?: AuthRepository.account.value?.id
    }

    fun refreshOrders() {
        triggerRefresh()
    }

    private fun triggerRefresh() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    override fun observeOrders(role: UserRole, tab: OrderTab): Flow<List<Order>> {
        return refreshTrigger.map {
            fetchOrdersFromSupabase(role, tab)
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun fetchOrdersFromSupabase(role: UserRole, tab: OrderTab): List<Order> {
        val userId = currentUserId()
        return try {
            client.postgrest
                .from("orders")
                .select {
                    filter {
                        // User: hanya pesanan miliknya.
                        // Admin/Penyedia: semua pesanan yang diizinkan RLS (provider_id = dia atau role ADMIN).
                        if (role == UserRole.USER && !userId.isNullOrBlank()) {
                            eq("user_id", userId)
                        }
                    }
                }
                .decodeList<Order>()
                .filter { tab.accepts(it.status) }
        } catch (e: Exception) {
            Log.e("SupabaseTrackingRepo", "Gagal memuat orders: ${e.message}", e)
            emptyList()
        }
    }

    override fun observeOrder(orderId: String): Flow<OrderDetail?> {
        return refreshTrigger.map {
            fetchOrderDetailFromSupabase(orderId)
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun fetchOrderDetailFromSupabase(orderId: String): OrderDetail? {
        return try {
            val order = client.postgrest
                .from("orders")
                .select { filter { eq("id", orderId) } }
                .decodeSingleOrNull<Order>() ?: return null

            val history = try {
                client.postgrest
                    .from("order_status_history")
                    .select { filter { eq("order_id", orderId) } }
                    .decodeList<StatusHistory>()
                    .sortedBy { it.createdAt ?: "" }
                    .map { h ->
                        if (h.timeText.isBlank() && !h.createdAt.isNullOrBlank())
                            h.copy(timeText = formatTimestamp(h.createdAt)) else h
                    }
            } catch (e: Exception) {
                Log.e("SupabaseTrackingRepo", "Gagal memuat riwayat status: ${e.message}", e)
                emptyList()
            }

            OrderDetail(order, history)
        } catch (e: Exception) {
            Log.e("SupabaseTrackingRepo", "Gagal memuat detail order: ${e.message}", e)
            null
        }
    }

    override suspend fun updateStatus(orderId: String, newStatus: OrderStatus, note: String?) {
        withContext(Dispatchers.IO) {
            try {
                // Trigger database mencatat perubahan ke order_status_history.
                client.postgrest
                    .from("orders")
                    .update(
                        OrderStatusUpdateDto(
                            status = newStatus,
                            progressPercent = if (newStatus == OrderStatus.SELESAI) 100 else null,
                        )
                    ) { filter { eq("id", orderId) } }
            } catch (e: Exception) {
                Log.e("SupabaseTrackingRepo", "Gagal update status: ${e.message}", e)
            }
            triggerRefresh()
        }
    }

    override suspend fun cancelOrder(orderId: String) {
        withContext(Dispatchers.IO) {
            try {
                client.postgrest
                    .from("orders")
                    .update(OrderStatusUpdateDto(status = OrderStatus.DIBATALKAN)) {
                        filter { eq("id", orderId) }
                    }
            } catch (e: Exception) {
                Log.e("SupabaseTrackingRepo", "Gagal membatalkan order: ${e.message}", e)
            }
            triggerRefresh()
        }
    }

    /** Menambahkan pesanan baru dari hasil checkout keranjang ke Supabase `orders`. */
    suspend fun addOrdersFromCart(items: List<CartItem>, defaultAddress: String = "Kost Putri Dahlia Kamar 204, Jl. Babarsari No. 12, Sleman, Yogyakarta") {
        withContext(Dispatchers.IO) {
            val userId = currentUserId() ?: "user-1"
            val userName = AuthRepository.account.value?.name ?: "Mahasiswa GenkSuruh"
            val userPhone = AuthRepository.account.value?.phone ?: "0812-3456-7890"
            val timeNow = SimpleDateFormat("d MMM yyyy • HH:mm 'WIB'", Locale.forLanguageTag("id-ID")).format(Date())

            val newOrders = items.mapIndexed { index, item ->
                val orderId = "ORD-2026-${(System.currentTimeMillis() % 10000) + index}"
                val category = when {
                    item.category.contains("Makanan", ignoreCase = true) || item.category.contains("Jajan", ignoreCase = true) -> ServiceCategory.ANTAR_MAKANAN
                    item.category.contains("Print", ignoreCase = true) || item.category.contains("Kreatif", ignoreCase = true) -> ServiceCategory.KREATIF
                    item.category.contains("Servis", ignoreCase = true) || item.category.contains("IT", ignoreCase = true) -> ServiceCategory.SERVIS_IT
                    item.category.contains("Acara", ignoreCase = true) -> ServiceCategory.EDUKASI
                    else -> ServiceCategory.HOME_SERVICE
                }

                Order(
                    id = orderId,
                    userId = userId,
                    providerId = "prov-1",
                    serviceId = item.serviceId,
                    category = category,
                    categoryLabel = item.category,
                    serviceName = item.serviceName,
                    packageName = "${item.quantity}x • ${item.date} • ${item.time}",
                    userName = userName,
                    userPhone = userPhone,
                    userAddress = item.location.ifBlank { defaultAddress },
                    providerName = item.providerName.ifBlank { "Runner Kampus" },
                    providerCompany = "Mitra GenkSuruh",
                    providerRating = 4.9,
                    providerOrderInfo = "100+ order sukses",
                    scheduleText = timeNow,
                    customerNote = item.note.takeIf { it.isNotBlank() },
                    servicePrice = item.price * item.quantity,
                    status = OrderStatus.MENUNGGU,
                )
            }

            try {
                client.postgrest
                    .from("orders")
                    .insert(newOrders)
            } catch (_: Exception) {
            }

            triggerRefresh()
        }
    }

    private fun formatTimestamp(isoString: String): String {
        return try {
            val odt = OffsetDateTime.parse(isoString)
            odt.format(DateTimeFormatter.ofPattern("HH:mm 'WIB'", Locale("id", "ID")))
        } catch (_: Exception) {
            SimpleDateFormat("HH:mm 'WIB'", Locale.forLanguageTag("id-ID")).format(Date())
        }
    }
}