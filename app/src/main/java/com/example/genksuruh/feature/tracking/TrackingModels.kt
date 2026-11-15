package com.example.genksuruh.feature.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.util.Locale

enum class UserRole { USER, PROVIDER }

/**
 * DEMO: peran aktif. Ketuk avatar di layar riwayat untuk berpindah User <-> Penyedia.
 * Nanti ganti dengan peran dari sesi login.
 */
object TrackingSession {
    var role by mutableStateOf(UserRole.USER)
}

/** Nilai status disepakati di satu tempat ini (kontrak dengan fitur Checkout/Keranjang). */
enum class OrderStatus(val label: String) {
    MENUNGGU("Menunggu"),
    DITERIMA("Diterima"),
    DIKERJAKAN("Dikerjakan"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    val isActive: Boolean get() = this == MENUNGGU || this == DITERIMA || this == DIKERJAKAN
    val canBeCancelled: Boolean get() = this == MENUNGGU || this == DITERIMA
}

enum class OrderTab(val title: String) {
    BERJALAN("Berjalan"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    fun accepts(status: OrderStatus): Boolean = when (this) {
        BERJALAN -> status.isActive
        SELESAI -> status == OrderStatus.SELESAI
        DIBATALKAN -> status == OrderStatus.DIBATALKAN
    }
}

enum class ServiceCategory { SERVIS_IT, EDUKASI, KREATIF, HOME_SERVICE, ANTAR_MAKANAN }

/** Padanan tabel `orders`. */
@Serializable
data class Order(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("service_id")
    val serviceId: String = "",
    val category: ServiceCategory = ServiceCategory.ANTAR_MAKANAN,
    @SerialName("category_label")
    val categoryLabel: String,
    @SerialName("service_name")
    val serviceName: String,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("user_phone")
    val userPhone: String,
    @SerialName("user_address")
    val userAddress: String,
    @SerialName("provider_name")
    val providerName: String,
    @SerialName("provider_company")
    val providerCompany: String = "",
    @SerialName("provider_rating")
    val providerRating: Double = 0.0,
    @SerialName("provider_order_info")
    val providerOrderInfo: String = "",
    @SerialName("provider_phone")
    val providerPhone: String = "0811-2233-4455",
    @SerialName("schedule_text")
    val scheduleText: String,
    @SerialName("customer_note")
    val customerNote: String? = null,
    @SerialName("service_price")
    val servicePrice: Long,
    @SerialName("add_on_price")
    val addOnPrice: Long = 0,
    @SerialName("app_fee")
    val appFee: Long = 0,
    @SerialName("platform_cut")
    val platformCut: Long = 0,
    val quantity: Int = 1,
    @SerialName("discount_amount")
    val discountAmount: Long = 0L,
    val status: OrderStatus,
    @SerialName("progress_percent")
    val progressPercent: Int? = null,
) {
    val totalPrice: Long get() = servicePrice + addOnPrice + appFee - discountAmount
    val providerEarning: Long get() = servicePrice + addOnPrice - platformCut
}

/** Padanan tabel `status_history` (sumber timeline). */
@Serializable
data class StatusHistory(
    @SerialName("order_id")
    val orderId: String,
    val status: OrderStatus,
    val note: String?,
    @SerialName("time_text")
    val timeText: String,
)

data class OrderDetail(val order: Order, val history: List<StatusHistory>)

fun Long.toRupiah(): String =
    "Rp " + NumberFormat.getInstance(Locale.forLanguageTag("id-ID")).format(this)
