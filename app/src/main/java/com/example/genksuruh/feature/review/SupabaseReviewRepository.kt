
package com.example.genksuruh.feature.review

import com.example.genksuruh.core.network.SupabaseClient

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ==========================================
// DTO UNTUK MEMBACA DATA REVIEWS
// ==========================================

@Serializable
private data class ReviewRecordDto(
    val id: String,

    @SerialName("order_id")
    val orderId: String,

    @SerialName("service_id")
    val serviceId: String,

    @SerialName("user_id")
    val userId: String,

    val rating: Int,
    val comment: String,

    @SerialName("created_at")
    val createdAt: String
)

// ==========================================
// DTO UNTUK MEMBACA NAMA USER
// ==========================================

@Serializable
private data class ReviewProfileDto(
    val id: String,
    val name: String
)

// ==========================================
// DTO UNTUK INSERT REVIEW
// ==========================================

@Serializable
private data class ReviewInsertDto(
    @SerialName("order_id")
    val orderId: String,

    @SerialName("service_id")
    val serviceId: String,

    @SerialName("user_id")
    val userId: String,

    val rating: Int,
    val comment: String
)

// ==========================================
// DTO UNTUK UPDATE REVIEW
// ==========================================

@Serializable
private data class ReviewUpdateDto(
    val rating: Int,
    val comment: String
)

// ==========================================
// DTO UNTUK RESTORE REVIEW
// ==========================================

@Serializable
private data class ReviewRestoreDto(
    val id: String,

    @SerialName("order_id")
    val orderId: String,

    @SerialName("service_id")
    val serviceId: String,

    @SerialName("user_id")
    val userId: String,

    val rating: Int,
    val comment: String,

    @SerialName("created_at")
    val createdAt: String
)

// ==========================================
// SUPABASE REVIEW REPOSITORY
// ==========================================


object SupabaseReviewRepository : ReviewRepository {

    private val client = SupabaseClient.client

    // ======================================
    // MENGAMBIL USER ID DARI SESI AKTIF
    // ======================================

    private fun currentUserId(): String {
        return client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException(
                "Sesi login tidak ditemukan. Silakan login kembali."
            )
    }

    // ======================================
    // MENGAMBIL PESANAN UNTUK REVIEW
    // ======================================

    override suspend fun getCompletedOrders(): List<ReviewOrderDto> {
        val userId = currentUserId()

        return client.postgrest
            .from("orders")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("status", "SELESAI")
                }
            }
            .decodeList<ReviewOrderDto>()
    }
    override suspend fun getOrderForReview(
        orderId: String
    ): ReviewOrderDto? {

        if (orderId.isBlank()) {
            return null
        }

        val userId = currentUserId()

        return client.postgrest
            .from("orders")
            .select {
                filter {
                    eq("id", orderId)
                    eq("user_id", userId)
                    eq("status", "SELESAI")
                }
            }
            .decodeSingleOrNull<ReviewOrderDto>()
    }


    override fun observeOwnReview(
        orderId: String
    ): kotlinx.coroutines.flow.Flow<Review?> =
        kotlinx.coroutines.flow.flow {

            val userId = currentUserId()

            val record = client
                .from("reviews")
                .select {
                    filter {
                        eq("order_id", orderId)
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<ReviewRecordDto>()

            if (record == null) {
                emit(null)
            } else {

                val profile = client
                    .from("profiles")
                    .select {
                        filter {
                            eq("id", record.userId)
                        }
                    }
                    .decodeSingleOrNull<ReviewProfileDto>()

                emit(
                    Review(
                        id = record.id,
                        userId = record.userId,
                        serviceId = record.serviceId,
                        orderId = record.orderId,
                        name = profile?.name ?: "Pengguna",
                        rating = record.rating,
                        comment = record.comment,
                        date = formatDate(record.createdAt),
                        isOwn = true,
                        createdAtIso = record.createdAt
                    )
                )
            }
        }

    // ======================================
    // MENGAMBIL DAFTAR REVIEW BERDASARKAN JASA
    // ======================================

    override suspend fun getReviews(
        serviceId: String
    ): List<Review> {

        val userId = currentUserId()

        val records = client.postgrest
            .from("reviews")
            .select {
                filter {
                    eq("service_id", serviceId)
                }
            }
            .decodeList<ReviewRecordDto>()
            .sortedByDescending { it.createdAt }

        return records.map { record ->

            // Mengambil nama pemberi ulasan dari profiles
            val profile = client.postgrest
                .from("profiles")
                .select {
                    filter {
                        eq("id", record.userId)
                    }
                }
                .decodeSingleOrNull<ReviewProfileDto>()

            Review(
                id = record.id,
                userId = record.userId,
                serviceId = record.serviceId,
                orderId = record.orderId,
                name = profile?.name ?: "Pengguna",
                rating = record.rating,
                comment = record.comment,
                date = formatDate(record.createdAt),
                isOwn = record.userId == userId,

                // Menyimpan timestamp asli Supabase
                createdAtIso = record.createdAt
            )
        }
    }

    // ======================================
    // MENAMBAH ATAU MENGEDIT REVIEW
    // ======================================

    override suspend fun submitReview(
        orderId: String,
        rating: Int,
        comment: String
    ): Review {

        require(rating in 1..5) {
            "Rating harus antara 1 sampai 5."
        }

        val cleanComment = comment.trim()

        require(cleanComment.length in 10..300) {
            "Komentar harus terdiri dari 10 sampai 300 karakter."
        }

        val userId = currentUserId()

        // Validasi pesanan langsung dari database
        val order = getOrderForReview(orderId)
            ?: throw IllegalStateException(
                "Pesanan tidak ditemukan, bukan milik kamu, atau belum selesai."
            )

        // Memeriksa apakah pesanan sudah pernah mendapatkan ulasan
        val existing = client.postgrest
            .from("reviews")
            .select {
                filter {
                    eq("order_id", orderId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<ReviewRecordDto>()

        if (existing == null) {

            // INSERT REVIEW BARU
            client.postgrest
                .from("reviews")
                .insert(
                    ReviewInsertDto(
                        orderId = order.id,
                        serviceId = order.serviceId,
                        userId = userId,
                        rating = rating,
                        comment = cleanComment
                    )
                )

        } else {

            // UPDATE REVIEW YANG SUDAH ADA
            client.postgrest
                .from("reviews")
                .update(
                    ReviewUpdateDto(
                        rating = rating,
                        comment = cleanComment
                    )
                ) {
                    filter {
                        eq("id", existing.id)
                        eq("user_id", userId)
                    }
                }
        }

        // Mengambil kembali data terbaru setelah INSERT / UPDATE
        val saved = client.postgrest
            .from("reviews")
            .select {
                filter {
                    eq("order_id", orderId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<ReviewRecordDto>()
            ?: throw IllegalStateException(
                "Ulasan berhasil dikirim tetapi gagal dibaca kembali."
            )

        // Mengambil nama user aktif
        val profile = client.postgrest
            .from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<ReviewProfileDto>()

        return Review(
            id = saved.id,
            userId = saved.userId,
            serviceId = saved.serviceId,
            orderId = saved.orderId,
            name = profile?.name ?: "Pengguna",
            rating = saved.rating,
            comment = saved.comment,
            date = formatDate(saved.createdAt),
            isOwn = true,

            // Menyimpan timestamp asli Supabase
            createdAtIso = saved.createdAt
        )
    }

    // ======================================
    // MENGHAPUS REVIEW
    // ======================================

    override suspend fun deleteReview(
        review: Review
    ) {

        val userId = currentUserId()

        require(review.userId == userId) {
            "Kamu hanya bisa menghapus ulasan milik sendiri."
        }

        client.postgrest
            .from("reviews")
            .delete {
                filter {
                    eq("id", review.id)
                    eq("user_id", userId)
                }
            }
    }

    // ======================================
    // RESTORE REVIEW SETELAH DIHAPUS
    // ======================================

    override suspend fun restoreReview(
        review: Review
    ) {

        val userId = currentUserId()

        require(review.userId == userId) {
            "Ulasan bukan milik user aktif."
        }

        val orderId = review.orderId
            ?: throw IllegalStateException(
                "Order ID tidak ditemukan."
            )

        // Memastikan pesanan masih memenuhi syarat
        val order = getOrderForReview(orderId)
            ?: throw IllegalStateException(
                "Pesanan tidak ditemukan atau tidak lagi memenuhi syarat review."
            )

        client.postgrest
            .from("reviews")
            .insert(
                ReviewRestoreDto(
                    id = review.id,
                    orderId = order.id,
                    serviceId = order.serviceId,
                    userId = userId,
                    rating = review.rating,
                    comment = review.comment,

                    // Menggunakan timestamp asli
                    // Jika kosong, gunakan waktu sekarang
                    createdAt = review.createdAtIso.ifBlank {
                        java.time.Instant.now().toString()
                    }
                )
            )
    }

    // ======================================
    // FORMAT TANGGAL UNTUK TAMPILAN UI
    // ======================================

    private fun formatDate(
        value: String
    ): String {

        return try {

            OffsetDateTime.parse(value).format(
                DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    Locale("id", "ID")
                )
            )

        } catch (e: Exception) {

            value
        }
    }
}
