package com.example.genksuruh.feature.tracking

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Kontrak data fitur Status & Riwayat. Nanti bisa diganti implementasi Room/Firebase. */
interface TrackingRepository {
    fun observeOrders(role: UserRole, tab: OrderTab): Flow<List<Order>>
    fun observeOrder(orderId: String): Flow<OrderDetail?>
    suspend fun updateStatus(orderId: String, newStatus: OrderStatus, note: String?)
    suspend fun cancelOrder(orderId: String)
}

object FakeTrackingRepository : TrackingRepository {

    private val orders = MutableStateFlow(dummyOrders())
    private val histories = MutableStateFlow(dummyHistories())

    // Data dummy: semua pesanan terlihat oleh kedua peran.
    // Implementasi asli: filter user_id (User) / provider_id (Penyedia).
    override fun observeOrders(role: UserRole, tab: OrderTab): Flow<List<Order>> =
        orders
            .map { list -> list.filter { tab.accepts(it.status) } }
            .onStart { delay(900) } // simulasi loading supaya skeleton terlihat

    override fun observeOrder(orderId: String): Flow<OrderDetail?> =
        combine(orders, histories) { o, h ->
            o.firstOrNull { it.id == orderId }?.let { OrderDetail(it, h[orderId].orEmpty()) }
        }

    override suspend fun updateStatus(orderId: String, newStatus: OrderStatus, note: String?) {
        delay(300)
        orders.update { list ->
            list.map {
                if (it.id != orderId) it
                else it.copy(
                    status = newStatus,
                    progressPercent = if (newStatus == OrderStatus.SELESAI) 100 else it.progressPercent,
                )
            }
        }
        addHistory(orderId, newStatus, note)
    }

    override suspend fun cancelOrder(orderId: String) {
        delay(300)
        orders.update { list ->
            list.map { if (it.id == orderId) it.copy(status = OrderStatus.DIBATALKAN) else it }
        }
        addHistory(orderId, OrderStatus.DIBATALKAN, "Pesanan dibatalkan oleh pemesan")
    }

    private fun addHistory(orderId: String, status: OrderStatus, note: String?) {
        val entry = StatusHistory(orderId, status, note?.takeIf { it.isNotBlank() }, nowText())
        histories.update { map -> map + (orderId to (map[orderId].orEmpty() + entry)) }
    }

    private fun nowText(): String =
        SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date()) + " WIB"

    // ------------------------------------------------------------------ data dummy
    private const val USER = "user-1"
    private const val PROVIDER = "prov-1"
    private const val ADDRESS = "Kost Putri Dahlia Kamar 204, Jl. Babarsari No. 12, Sleman, Yogyakarta"

    private fun dummyOrders() = listOf(
        Order(
            id = "ORD-2025-0015", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.SERVIS_IT, categoryLabel = "Servis & IT",
            serviceName = "Jasa Servis Laptop & Komputer",
            packageName = "Cek & perbaikan laptop (maks. 2 jam)",
            userName = "Anisa Rahmawati", userPhone = "0812-3456-7890", userAddress = ADDRESS,
            providerName = "Budi Santoso", providerCompany = "TechFix Solutions",
            providerRating = 4.8, providerOrderInfo = "95+ order sukses",
            scheduleText = "24 Okt 2024, 10:00 WIB",
            servicePrice = 150_000, status = OrderStatus.MENUNGGU,
        ),
        Order(
            id = "ORD-2025-0014", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.EDUKASI, categoryLabel = "Edukasi",
            serviceName = "Les Privat Matematika",
            packageName = "Sesi privat 90 menit (Online Meet)",
            userName = "Rizky Pratama", userPhone = "0813-2211-4455", userAddress = "Online (Google Meet)",
            providerName = "Farhan, M.Si.", providerCompany = "Tutor Terverifikasi",
            providerRating = 4.9, providerOrderInfo = "210+ sesi",
            scheduleText = "24 Okt 2024, 14:00 WIB (Online Meet)",
            servicePrice = 120_000, status = OrderStatus.DITERIMA,
        ),
        Order(
            id = "ORD-2025-0013", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.KREATIF, categoryLabel = "Kreatif & Desain",
            serviceName = "Desain Presentasi Profesional",
            packageName = "Desain 10 slide + 3x revisi",
            userName = "Dewi Lestari", userPhone = "0857-1000-2233", userAddress = "Online",
            providerName = "Siti Aminah", providerCompany = "Studio Kreatif Nusantara",
            providerRating = 4.7, providerOrderInfo = "80+ order sukses",
            scheduleText = "Mulai 23 Okt 2024, 09:30 WIB",
            servicePrice = 85_000, status = OrderStatus.DIKERJAKAN, progressPercent = 70,
        ),
        Order(
            id = "ORD-2025-0012", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.HOME_SERVICE, categoryLabel = "Home Service",
            serviceName = "Servis AC Kamar & Pembersihan",
            packageName = "Paket Cuci AC 1/2 - 1 PK + Tambah Freon",
            userName = "Anisa Rahmawati", userPhone = "0812-3456-7890", userAddress = ADDRESS,
            providerName = "Budi Santoso", providerCompany = "Sejuk Jaya Mandiri",
            providerRating = 4.9, providerOrderInfo = "120+ order sukses",
            scheduleText = "Hari ini, 24 Okt 2024 (14:00 - 15:30 WIB)",
            customerNote = "Mohon teknisi membawa tangga lipat, AC di atas lemari.",
            servicePrice = 75_000, addOnPrice = 50_000, appFee = 2_000, platformCut = 5_000,
            status = OrderStatus.DIKERJAKAN,
        ),
        Order(
            id = "ORD-2025-0009", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.HOME_SERVICE, categoryLabel = "Home Service",
            serviceName = "Servis AC Kamar & Pembersihan",
            packageName = "Paket Cuci AC 1/2 - 1 PK",
            userName = "Bagas Saputra", userPhone = "0821-9988-7766", userAddress = ADDRESS,
            providerName = "Budi Santoso", providerCompany = "Sejuk Jaya Mandiri",
            providerRating = 4.9, providerOrderInfo = "120+ order sukses",
            scheduleText = "Selesai 17 Okt 2024, 15:30 WIB",
            servicePrice = 88_000, appFee = 2_000, platformCut = 4_000,
            status = OrderStatus.SELESAI, progressPercent = 100,
        ),
    )

    private fun h(id: String, s: OrderStatus, time: String, note: String? = null) =
        StatusHistory(id, s, note, time)

    private fun dummyHistories(): Map<String, List<StatusHistory>> = mapOf(
        "ORD-2025-0015" to listOf(
            h("ORD-2025-0015", OrderStatus.MENUNGGU, "08:10 WIB", "Pesanan masuk, menunggu konfirmasi penyedia jasa"),
        ),
        "ORD-2025-0014" to listOf(
            h("ORD-2025-0014", OrderStatus.MENUNGGU, "07:45 WIB", "Pesanan dibuat"),
            h("ORD-2025-0014", OrderStatus.DITERIMA, "08:05 WIB", "Tutor menyetujui jadwal sesi"),
        ),
        "ORD-2025-0013" to listOf(
            h("ORD-2025-0013", OrderStatus.MENUNGGU, "09:00 WIB", "Pesanan dibuat"),
            h("ORD-2025-0013", OrderStatus.DITERIMA, "09:10 WIB", "Studio menyetujui pesanan"),
            h("ORD-2025-0013", OrderStatus.DIKERJAKAN, "09:30 WIB", "Draft akhir sedang dikerjakan"),
        ),
        "ORD-2025-0012" to listOf(
            h("ORD-2025-0012", OrderStatus.MENUNGGU, "09:30 WIB", "Menunggu konfirmasi penyedia jasa"),
            h("ORD-2025-0012", OrderStatus.DITERIMA, "09:45 WIB", "Mitra menyetujui jadwal dan menuju lokasi"),
            h("ORD-2025-0012", OrderStatus.DIKERJAKAN, "14:15 WIB", "Penyedia sedang membersihkan unit indoor & periksa tekanan kompresor."),
        ),
        "ORD-2025-0009" to listOf(
            h("ORD-2025-0009", OrderStatus.MENUNGGU, "09:30 WIB", "Pesanan berhasil dikonfirmasi sistem"),
            h("ORD-2025-0009", OrderStatus.DITERIMA, "09:45 WIB", "Teknisi Budi Santoso bersiap ke lokasi"),
            h("ORD-2025-0009", OrderStatus.DIKERJAKAN, "14:15 WIB", "Pembersihan unit AC dimulai"),
            h("ORD-2025-0009", OrderStatus.SELESAI, "15:30 WIB", "Pembersihan unit AC & pengisian freon tuntas"),
        ),
    )
}