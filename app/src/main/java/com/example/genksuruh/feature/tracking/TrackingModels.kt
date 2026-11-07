package com.example.genksuruh.feature.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
data class Order(
    val id: String,
    val userId: String,
    val providerId: String,
    val category: ServiceCategory,
    val categoryLabel: String,
    val serviceName: String,
    val packageName: String,
    val userName: String,
    val userPhone: String,
    val userAddress: String,
    val providerName: String,
    val providerCompany: String,
    val providerRating: Double,
    val providerOrderInfo: String,
    val providerPhone: String = "0811-2233-4455",
    val scheduleText: String,
    val customerNote: String? = null,
    val servicePrice: Long,
    val addOnPrice: Long = 0,
    val appFee: Long = 0,
    val platformCut: Long = 0,
    val status: OrderStatus,
    val progressPercent: Int? = null,
) {
    val totalPrice: Long get() = servicePrice + addOnPrice + appFee
    val providerEarning: Long get() = servicePrice + addOnPrice - platformCut
}

/** Padanan tabel `status_history` (sumber timeline). */
data class StatusHistory(
    val orderId: String,
    val status: OrderStatus,
    val note: String?,
    val timeText: String,
)

data class OrderDetail(val order: Order, val history: List<StatusHistory>)

fun Long.toRupiah(): String =
    "Rp " + NumberFormat.getInstance(Locale.forLanguageTag("id-ID")).format(this)