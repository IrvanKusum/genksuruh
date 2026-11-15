package com.example.genksuruh.feature.review

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// Kontrak Repository Ulasan
interface ReviewRepository {
    fun observeOwnReview(orderId: String): Flow<Review?>
    fun hasOwnReview(orderId: String): Boolean
    fun getInitialReviews(orderId: String, serviceName: String): List<Review>
    fun getInitialDistribution(serviceName: String, orderId: String): Map<Int, Int>
    fun saveOwnReview(review: Review)
    fun deleteOwnReview(orderId: String)

    companion object : ReviewRepository by DummyReviewRepository
}

/**
 * Implementasi Dummy / In-Memory ReviewRepository.
 */
object DummyReviewRepository : ReviewRepository {

    private val _ownReviewsByOrder = MutableStateFlow<Map<String, Review>>(emptyMap())

    override fun observeOwnReview(orderId: String): Flow<Review?> =
        _ownReviewsByOrder.map { reviews -> reviews[orderId] }

    override fun hasOwnReview(orderId: String): Boolean = _ownReviewsByOrder.value.containsKey(orderId)

    private val foodReviews = listOf(
        Review("rev-1", "usr-nadia", "svc-1", null, "Nadia Putri", 5, "Pesanan datang tepat waktu dan makanan masih hangat. Mitranya juga ramah.", "28 Okt 2026"),
        Review("rev-2", "usr-rafi", "svc-1", null, "Rafi Ahmad", 4, "Pelayanannya bagus dan proses pemesanan mudah.", "25 Okt 2026"),
        Review("rev-3", "usr-salsa", "svc-1", null, "Salsa Maharani", 5, "Sangat membantu dan sesuai dengan yang saya butuhkan.", "22 Okt 2026"),
        Review("rev-4", "usr-bagas", "svc-1", null, "Bagas Saputra", 3, "Layanan cukup baik, semoga waktu tunggunya bisa lebih singkat.", "19 Okt 2026"),
        Review("rev-5", "usr-citra", "svc-1", null, "Citra Lestari", 5, "Mitra ramah dan informasinya jelas. Terima kasih!", "15 Okt 2026"),
    )

    private val generalReviews = listOf(
        Review("rev-1", "usr-nadia", "svc-gen", null, "Nadia Putri", 5, "Layanan berjalan lancar dan hasilnya sesuai dengan kebutuhan saya.", "28 Okt 2026"),
        Review("rev-2", "usr-rafi", "svc-gen", null, "Rafi Ahmad", 4, "Penyedia ramah, komunikatif, dan prosesnya cukup cepat.", "25 Okt 2026"),
        Review("rev-3", "usr-salsa", "svc-gen", null, "Salsa Maharani", 5, "Hasil pekerjaan bagus dan sesuai dengan deskripsi layanan.", "22 Okt 2026"),
        Review("rev-4", "usr-bagas", "svc-gen", null, "Bagas Saputra", 4, "Pengalaman menggunakan layanan ini cukup memuaskan.", "19 Okt 2026"),
    )

    override fun getInitialReviews(orderId: String, serviceName: String): List<Review> {
        val source = if (serviceName.contains("Antar Makanan", ignoreCase = true)) foodReviews else generalReviews
        val seeded = source.map { it.copy(orderId = orderId) }
        val own = _ownReviewsByOrder.value[orderId]
        return if (own == null) seeded else listOf(own) + seeded
    }

    override fun getInitialDistribution(serviceName: String, orderId: String): Map<Int, Int> {
        val base = if (serviceName.contains("Antar Makanan", ignoreCase = true)) {
            mapOf(5 to 105, 4 to 14, 3 to 3, 2 to 1, 1 to 1)
        } else {
            mapOf(5 to 24, 4 to 8, 3 to 2, 2 to 0, 1 to 0)
        }
        val own = _ownReviewsByOrder.value[orderId] ?: return base
        return base + (own.rating to ((base[own.rating] ?: 0) + 1))
    }

    override fun saveOwnReview(review: Review) {
        val id = review.orderId ?: return
        _ownReviewsByOrder.value = _ownReviewsByOrder.value + (id to review.copy(isOwn = true))
    }

    override fun deleteOwnReview(orderId: String) {
        _ownReviewsByOrder.value = _ownReviewsByOrder.value - orderId
    }
}
