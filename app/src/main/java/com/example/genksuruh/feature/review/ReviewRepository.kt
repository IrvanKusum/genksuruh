package com.example.genksuruh.feature.review

import kotlinx.coroutines.flow.map

object ReviewRepository {
    private val _ownReviewsByOrder = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Review>>(emptyMap())

    /** Memantau ulasan milik pengguna untuk satu pesanan secara reaktif. */
    fun observeOwnReview(orderId: String): kotlinx.coroutines.flow.Flow<Review?> =
        _ownReviewsByOrder.map { reviews -> reviews[orderId] }

    fun hasOwnReview(orderId: String): Boolean = _ownReviewsByOrder.value.containsKey(orderId)

    private val foodReviews = listOf(
        Review(1, "Nadia Putri", 5, "Pesanan datang tepat waktu dan makanan masih hangat. Mitranya juga ramah.", "28 Okt 2026"),
        Review(2, "Rafi Ahmad", 4, "Pelayanannya bagus dan proses pemesanan mudah.", "25 Okt 2026"),
        Review(3, "Salsa Maharani", 5, "Sangat membantu dan sesuai dengan yang saya butuhkan.", "22 Okt 2026"),
        Review(4, "Bagas Saputra", 3, "Layanan cukup baik, semoga waktu tunggunya bisa lebih singkat.", "19 Okt 2026"),
        Review(5, "Citra Lestari", 5, "Mitra ramah dan informasinya jelas. Terima kasih!", "15 Okt 2026"),
    )

    private val generalReviews = listOf(
        Review(1, "Nadia Putri", 5, "Layanan berjalan lancar dan hasilnya sesuai dengan kebutuhan saya.", "28 Okt 2026"),
        Review(2, "Rafi Ahmad", 4, "Penyedia ramah, komunikatif, dan prosesnya cukup cepat.", "25 Okt 2026"),
        Review(3, "Salsa Maharani", 5, "Hasil pekerjaan bagus dan sesuai dengan deskripsi layanan.", "22 Okt 2026"),
        Review(4, "Bagas Saputra", 4, "Pengalaman menggunakan layanan ini cukup memuaskan.", "19 Okt 2026"),
    )

    fun getInitialReviews(orderId: String, serviceName: String): List<Review> {
        val source = if (serviceName.contains("Antar Makanan", ignoreCase = true)) foodReviews else generalReviews
        val seeded = source.map { it.copy(orderId = orderId) }
        val own = _ownReviewsByOrder.value[orderId]
        return if (own == null) seeded else listOf(own) + seeded
    }

    fun getInitialDistribution(serviceName: String, orderId: String): Map<Int, Int> {
        val base = if (serviceName.contains("Antar Makanan", ignoreCase = true)) {
            mapOf(5 to 105, 4 to 14, 3 to 3, 2 to 1, 1 to 1)
        } else {
            mapOf(5 to 24, 4 to 8, 3 to 2, 2 to 0, 1 to 0)
        }
        val own = _ownReviewsByOrder.value[orderId] ?: return base
        return base + (own.rating to ((base[own.rating] ?: 0) + 1))
    }

    fun saveOwnReview(review: Review) {
        val id = review.orderId ?: return
        _ownReviewsByOrder.value = _ownReviewsByOrder.value + (id to review.copy(isOwn = true))
    }

    fun deleteOwnReview(orderId: String) {
        _ownReviewsByOrder.value = _ownReviewsByOrder.value - orderId
    }
}
