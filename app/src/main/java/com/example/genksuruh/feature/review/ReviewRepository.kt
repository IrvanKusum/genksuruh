package com.example.genksuruh.feature.review

class ReviewRepository {
    private val initialReviews = listOf(
        Review(1, "Irvan", 5, "Cepat banget, makanan masih hangat pas sampai di perpus. Dimas ramah dan selalu kabarin lewat chat.", "28 Okt 2026", true),
        Review(2, "Nadia Putri", 5, "Langganan tiap hari Senin. Nggak pernah telat, harganya juga murah banget buat anak kos.", "25 Okt 2026"),
        Review(3, "Rafi Ahmad", 4, "Overall bagus, cuma pas jam makan siang agak lama nunggu karena kantin rame.", "22 Okt 2026"),
        Review(4, "Salsa Maharani", 5, "Sangat membantu pas lagi deadline tugas kelompok. Recommended!", "19 Okt 2026"),
        Review(5, "Bagas Saputra", 3, "Makanannya sampai tapi es teh-nya tumpah sedikit. Semoga ke depan lebih hati-hati.", "15 Okt 2026"),
        Review(6, "Citra Lestari", 5, "Antarnya tepat waktu dan sopan. Pasti pesan lagi.", "10 Okt 2026"),
    )

    fun getInitialReviews(): List<Review> = initialReviews.toList()

    fun getInitialDistribution(): Map<Int, Int> = mapOf(5 to 105, 4 to 14, 3 to 3, 2 to 1, 1 to 1)
}
