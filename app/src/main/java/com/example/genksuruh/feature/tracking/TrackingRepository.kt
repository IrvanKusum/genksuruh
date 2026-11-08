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

    /** Snapshot untuk mengisi informasi jasa pada halaman ulasan berdasarkan pesanan terpilih. */
    fun findOrder(orderId: String): Order? = orders.value.firstOrNull { it.id == orderId }

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
        // BERJALAN (7 pesanan)
        Order(
            id = "ORD-2025-0015", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.SERVIS_IT, categoryLabel = "Servis & IT",
            serviceName = "Jasa Servis Laptop & Komputer", packageName = "Cek & perbaikan laptop (maks. 2 jam)",
            userName = "Anisa Rahmawati", userPhone = "0812-3456-7890", userAddress = ADDRESS,
            providerName = "Budi Santoso", providerCompany = "TechFix Solutions", providerRating = 4.8,
            providerOrderInfo = "95+ order sukses", scheduleText = "24 Okt 2024, 10:00 WIB",
            servicePrice = 150_000, status = OrderStatus.MENUNGGU,
        ),
        Order(
            id = "ORD-2025-0014", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.EDUKASI, categoryLabel = "Edukasi",
            serviceName = "Les Privat Matematika", packageName = "Sesi privat 90 menit (Online Meet)",
            userName = "Rizky Pratama", userPhone = "0813-2211-4455", userAddress = "Online (Google Meet)",
            providerName = "Farhan, M.Si.", providerCompany = "Tutor Terverifikasi", providerRating = 4.9,
            providerOrderInfo = "210+ sesi", scheduleText = "24 Okt 2024, 14:00 WIB",
            servicePrice = 120_000, status = OrderStatus.DITERIMA,
        ),
        Order(
            id = "ORD-2025-0013", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.KREATIF, categoryLabel = "Kreatif & Desain",
            serviceName = "Desain Presentasi Profesional", packageName = "Desain 10 slide + 3x revisi",
            userName = "Dewi Lestari", userPhone = "0857-1000-2233", userAddress = "Online",
            providerName = "Siti Aminah", providerCompany = "Studio Kreatif Nusantara", providerRating = 4.7,
            providerOrderInfo = "80+ order sukses", scheduleText = "Mulai 23 Okt 2024, 09:30 WIB",
            servicePrice = 85_000, status = OrderStatus.DIKERJAKAN, progressPercent = 70,
        ),
        Order(
            id = "ORD-2025-0012", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.HOME_SERVICE, categoryLabel = "Home Service",
            serviceName = "Servis AC Kamar & Pembersihan", packageName = "Paket Cuci AC 1/2 - 1 PK + Tambah Freon",
            userName = "Anisa Rahmawati", userPhone = "0812-3456-7890", userAddress = ADDRESS,
            providerName = "Budi Santoso", providerCompany = "Sejuk Jaya Mandiri", providerRating = 4.9,
            providerOrderInfo = "120+ order sukses", scheduleText = "Hari ini, 24 Okt 2024 (14:00 - 15:30 WIB)",
            customerNote = "Mohon teknisi membawa tangga lipat, AC di atas lemari.",
            servicePrice = 75_000, addOnPrice = 50_000, appFee = 2_000, platformCut = 5_000,
            status = OrderStatus.DIKERJAKAN,
        ),
        Order(
            id = "ORD-2025-0016", userId = USER, providerId = "prov-food",
            category = ServiceCategory.ANTAR_MAKANAN, categoryLabel = "Antar Makanan",
            serviceName = "Antar Makanan Kantin FILKOM", packageName = "Antar makanan ke area FILKOM",
            userName = "Ahsana Maswa", userPhone = "0812-1111-2222", userAddress = "FILKOM Universitas Brawijaya",
            providerName = "Dimas Pratama", providerCompany = "Mitra Antar Kantin FILKOM", providerRating = 4.9,
            providerOrderInfo = "150+ pesanan sukses", scheduleText = "Hari ini, 11:30 WIB",
            servicePrice = 8_000, appFee = 1_000, status = OrderStatus.DITERIMA,
        ),
        Order(
            id = "ORD-2025-0017", userId = USER, providerId = "prov-design",
            category = ServiceCategory.KREATIF, categoryLabel = "Kreatif & Desain",
            serviceName = "Desain Poster Acara", packageName = "1 poster digital + 2x revisi",
            userName = "Nadia Putri", userPhone = "0813-2222-3333", userAddress = "Online",
            providerName = "Maya Putri", providerCompany = "Maya Design Studio", providerRating = 4.8,
            providerOrderInfo = "65+ order sukses", scheduleText = "25 Okt 2024, 13:00 WIB",
            servicePrice = 65_000, status = OrderStatus.MENUNGGU,
        ),
        Order(
            id = "ORD-2025-0018", userId = USER, providerId = "prov-it",
            category = ServiceCategory.SERVIS_IT, categoryLabel = "Servis & IT",
            serviceName = "Instalasi Software Laptop", packageName = "Instalasi aplikasi dan optimasi sistem",
            userName = "Bagas Saputra", userPhone = "0821-9988-7766", userAddress = ADDRESS,
            providerName = "Reno Saputra", providerCompany = "TechFix Solutions", providerRating = 4.7,
            providerOrderInfo = "70+ order sukses", scheduleText = "26 Okt 2024, 09:00 WIB",
            servicePrice = 55_000, status = OrderStatus.MENUNGGU,
        ),

        // SELESAI (5 pesanan)
        Order(
            id = "ORD-2025-0009", userId = USER, providerId = PROVIDER,
            category = ServiceCategory.HOME_SERVICE, categoryLabel = "Home Service",
            serviceName = "Servis AC Kamar & Pembersihan", packageName = "Paket Cuci AC 1/2 - 1 PK",
            userName = "Bagas Saputra", userPhone = "0821-9988-7766", userAddress = ADDRESS,
            providerName = "Budi Santoso", providerCompany = "Sejuk Jaya Mandiri", providerRating = 4.9,
            providerOrderInfo = "120+ order sukses", scheduleText = "Selesai 17 Okt 2024, 15:30 WIB",
            servicePrice = 88_000, appFee = 2_000, platformCut = 4_000,
            status = OrderStatus.SELESAI, progressPercent = 100,
        ),
        Order(
            id = "ORD-2025-0020", userId = USER, providerId = "prov-food",
            category = ServiceCategory.ANTAR_MAKANAN, categoryLabel = "Antar Makanan",
            serviceName = "Antar Makanan Kantin FILKOM", packageName = "Antar nasi ayam + es teh",
            userName = "Ahsana Maswa", userPhone = "0812-1111-2222", userAddress = "Gedung F FILKOM UB",
            providerName = "Dimas Pratama", providerCompany = "Mitra Antar Kantin FILKOM", providerRating = 4.9,
            providerOrderInfo = "150+ pesanan sukses", scheduleText = "Selesai 20 Okt 2024, 12:15 WIB",
            servicePrice = 8_000, appFee = 1_000, status = OrderStatus.SELESAI, progressPercent = 100,
        ),
        Order(
            id = "ORD-2025-0021", userId = USER, providerId = "prov-tutor",
            category = ServiceCategory.EDUKASI, categoryLabel = "Edukasi",
            serviceName = "Les Privat Bahasa Inggris", packageName = "Sesi privat 60 menit",
            userName = "Citra Lestari", userPhone = "0812-3333-4444", userAddress = "Online (Google Meet)",
            providerName = "Alya Nurfadila", providerCompany = "Tutor Terverifikasi", providerRating = 4.9,
            providerOrderInfo = "90+ sesi", scheduleText = "Selesai 19 Okt 2024, 16:00 WIB",
            servicePrice = 90_000, status = OrderStatus.SELESAI, progressPercent = 100,
        ),
        Order(
            id = "ORD-2025-0022", userId = USER, providerId = "prov-it",
            category = ServiceCategory.SERVIS_IT, categoryLabel = "Servis & IT",
            serviceName = "Upgrade RAM Laptop", packageName = "Pemasangan RAM 8 GB",
            userName = "Rafi Ahmad", userPhone = "0812-4444-5555", userAddress = ADDRESS,
            providerName = "Reno Saputra", providerCompany = "TechFix Solutions", providerRating = 4.8,
            providerOrderInfo = "70+ order sukses", scheduleText = "Selesai 18 Okt 2024, 10:30 WIB",
            servicePrice = 175_000, status = OrderStatus.SELESAI, progressPercent = 100,
        ),
        Order(
            id = "ORD-2025-0023", userId = USER, providerId = "prov-creative",
            category = ServiceCategory.KREATIF, categoryLabel = "Kreatif & Desain",
            serviceName = "Edit Video Tugas", packageName = "Video maksimal 5 menit",
            userName = "Salsa Maharani", userPhone = "0812-5555-6666", userAddress = "Online",
            providerName = "Siti Aminah", providerCompany = "Studio Kreatif Nusantara", providerRating = 4.7,
            providerOrderInfo = "80+ order sukses", scheduleText = "Selesai 16 Okt 2024, 18:00 WIB",
            servicePrice = 100_000, status = OrderStatus.SELESAI, progressPercent = 100,
        ),

        // DIBATALKAN (3 pesanan)
        Order(
            id = "ORD-2025-0024", userId = USER, providerId = "prov-home",
            category = ServiceCategory.HOME_SERVICE, categoryLabel = "Home Service",
            serviceName = "Perbaikan Keran Air", packageName = "Pemeriksaan dan perbaikan keran",
            userName = "Nadia Putri", userPhone = "0813-2222-3333", userAddress = ADDRESS,
            providerName = "Agus Setiawan", providerCompany = "HomeCare Service", providerRating = 4.6,
            providerOrderInfo = "45+ order sukses", scheduleText = "Dibatalkan 15 Okt 2024, 09:00 WIB",
            servicePrice = 45_000, status = OrderStatus.DIBATALKAN,
        ),
        Order(
            id = "ORD-2025-0025", userId = USER, providerId = "prov-food",
            category = ServiceCategory.ANTAR_MAKANAN, categoryLabel = "Antar Makanan",
            serviceName = "Antar Makanan Kantin FILKOM", packageName = "Antar minuman dan camilan",
            userName = "Ahsana Maswa", userPhone = "0812-1111-2222", userAddress = "Perpustakaan FILKOM UB",
            providerName = "Dimas Pratama", providerCompany = "Mitra Antar Kantin FILKOM", providerRating = 4.9,
            providerOrderInfo = "150+ pesanan sukses", scheduleText = "Dibatalkan 14 Okt 2024, 13:00 WIB",
            servicePrice = 6_000, status = OrderStatus.DIBATALKAN,
        ),
        Order(
            id = "ORD-2025-0026", userId = USER, providerId = "prov-tutor",
            category = ServiceCategory.EDUKASI, categoryLabel = "Edukasi",
            serviceName = "Konsultasi Pemrograman", packageName = "Konsultasi 45 menit",
            userName = "Rizky Pratama", userPhone = "0813-2211-4455", userAddress = "Online",
            providerName = "Farhan, M.Si.", providerCompany = "Tutor Terverifikasi", providerRating = 4.9,
            providerOrderInfo = "210+ sesi", scheduleText = "Dibatalkan 13 Okt 2024, 15:00 WIB",
            servicePrice = 50_000, status = OrderStatus.DIBATALKAN,
        ),
    )

    private fun h(id: String, s: OrderStatus, time: String, note: String? = null) =
        StatusHistory(id, s, note, time)

    private fun dummyHistories(): Map<String, List<StatusHistory>> = mapOf(
        "ORD-2025-0015" to listOf(h("ORD-2025-0015", OrderStatus.MENUNGGU, "08:10 WIB", "Pesanan masuk, menunggu konfirmasi penyedia jasa")),
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
            h("ORD-2025-0012", OrderStatus.MENUNGGU, "09:30 WIB", "Pesanan dibuat"),
            h("ORD-2025-0012", OrderStatus.DITERIMA, "09:45 WIB", "Mitra menyetujui jadwal"),
            h("ORD-2025-0012", OrderStatus.DIKERJAKAN, "14:15 WIB", "Penyedia sedang membersihkan unit AC"),
        ),
        "ORD-2025-0016" to listOf(
            h("ORD-2025-0016", OrderStatus.MENUNGGU, "11:10 WIB", "Pesanan makanan dibuat"),
            h("ORD-2025-0016", OrderStatus.DITERIMA, "11:15 WIB", "Mitra kantin menerima pesanan"),
        ),
        "ORD-2025-0017" to listOf(h("ORD-2025-0017", OrderStatus.MENUNGGU, "13:00 WIB", "Menunggu konfirmasi desainer")),
        "ORD-2025-0018" to listOf(h("ORD-2025-0018", OrderStatus.MENUNGGU, "09:00 WIB", "Pesanan masuk")),
        "ORD-2025-0009" to listOf(
            h("ORD-2025-0009", OrderStatus.MENUNGGU, "09:30 WIB", "Pesanan dibuat"),
            h("ORD-2025-0009", OrderStatus.DITERIMA, "09:45 WIB", "Teknisi bersiap ke lokasi"),
            h("ORD-2025-0009", OrderStatus.DIKERJAKAN, "14:15 WIB", "Pembersihan unit AC dimulai"),
            h("ORD-2025-0009", OrderStatus.SELESAI, "15:30 WIB", "Pembersihan unit AC tuntas"),
        ),
        "ORD-2025-0020" to listOf(
            h("ORD-2025-0020", OrderStatus.MENUNGGU, "11:30 WIB", "Pesanan makanan dibuat"),
            h("ORD-2025-0020", OrderStatus.DITERIMA, "11:35 WIB", "Mitra menerima pesanan"),
            h("ORD-2025-0020", OrderStatus.DIKERJAKAN, "11:45 WIB", "Pesanan sedang disiapkan"),
            h("ORD-2025-0020", OrderStatus.SELESAI, "12:15 WIB", "Makanan berhasil diantarkan"),
        ),
        "ORD-2025-0021" to listOf(h("ORD-2025-0021", OrderStatus.SELESAI, "16:00 WIB", "Sesi les selesai")),
        "ORD-2025-0022" to listOf(h("ORD-2025-0022", OrderStatus.SELESAI, "10:30 WIB", "Upgrade RAM selesai")),
        "ORD-2025-0023" to listOf(h("ORD-2025-0023", OrderStatus.SELESAI, "18:00 WIB", "Video selesai dan diserahkan")),
        "ORD-2025-0024" to listOf(h("ORD-2025-0024", OrderStatus.DIBATALKAN, "09:00 WIB", "Dibatalkan oleh pemesan")),
        "ORD-2025-0025" to listOf(h("ORD-2025-0025", OrderStatus.DIBATALKAN, "13:00 WIB", "Dibatalkan oleh pemesan")),
        "ORD-2025-0026" to listOf(h("ORD-2025-0026", OrderStatus.DIBATALKAN, "15:00 WIB", "Dibatalkan oleh pemesan")),
    )
}
