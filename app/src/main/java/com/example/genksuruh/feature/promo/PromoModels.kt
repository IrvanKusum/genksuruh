package com.example.genksuruh.feature.promo

enum class DiscountType { PERSEN, NOMINAL }

enum class VoucherStatus(val label: String) {
    AKTIF("Aktif"),
    TERPAKAI("Terpakai"),
    KEDALUWARSA("Kedaluwarsa")
}

data class Promo(
    val id: String,
    val title: String,
    val code: String,
    val category: String,
    val emoji: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val maxDiscount: Int = 0,
    val minOrder: Int = 0,
    val serviceName: String,
    val providerName: String,   // nama yang tampil di UI
    val ownerId: String,        // pemilik promo (untuk mode Penyedia)
    val startDate: String,      // yyyy-MM-dd
    val endDate: String,        // yyyy-MM-dd
    val quota: Int,
    val claimed: Int,
    val description: String = "",
    val terms: List<String> = emptyList()
) {
    val isFull: Boolean get() = claimed >= quota
    val remaining: Int get() = (quota - claimed).coerceAtLeast(0)
    val isExpired: Boolean get() = endDate < PromoRepository.TODAY
    val badge: String
        get() = if (discountType == DiscountType.PERSEN) "$discountValue%"
        else "Rp ${discountValue / 1000}rb"
}

data class VoucherClaim(
    val id: String,
    val promoId: String,
    val userName: String,
    val status: VoucherStatus,
    val claimedAt: String,
    val usedAt: String? = null
)

data class DeletedSnapshot(val promo: Promo, val claims: List<VoucherClaim>)

data class ServiceOption(val name: String, val category: String, val emoji: String)

val serviceOptions = listOf(
    ServiceOption("Antar Makanan Kantin FILKOM", "Antar Makanan", "🍱"),
    ServiceOption("Titip Beli Jajan Indomaret", "Beli Jajan", "🛒"),
    ServiceOption("Temani Nonton Konser", "Temani Acara", "🎤"),
    ServiceOption("Ganti Gas Elpiji 3 Kg", "Bantu Rumah Tangga", "🔥"),
    ServiceOption("Fotokopi dan Jilid Skripsi", "Print dan Fotokopi", "🖨️"),
    ServiceOption("Antar Jemput Galon Air", "Bantu Rumah Tangga", "💧")
)

val promoCategories = listOf(
    "Semua", "Antar Makanan", "Beli Jajan", "Temani Acara", "Bantu Rumah Tangga"
)

val defaultTerms = listOf(
    "Satu voucher untuk satu pengguna.",
    "Tidak bisa digabung dengan promo lain.",
    "Berlaku selama periode dan kuota masih tersedia."
)