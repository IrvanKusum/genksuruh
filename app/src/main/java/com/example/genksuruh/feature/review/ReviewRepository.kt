
package com.example.genksuruh.feature.review

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ReviewRepository {

    suspend fun getCompletedOrders(): List<ReviewOrderDto>
    suspend fun getOrderForReview(
        orderId: String
    ): ReviewOrderDto?

    suspend fun getReviews(
        serviceId: String
    ): List<Review>

    suspend fun submitReview(
        orderId: String,
        rating: Int,
        comment: String
    ): Review

    suspend fun deleteReview(
        review: Review
    )

    suspend fun restoreReview(
        review: Review
    )

    // Mengamati ulasan milik user berdasarkan order ID.
    fun observeOwnReview(
        orderId: String
    ): Flow<Review?>
}

object DummyReviewRepository : ReviewRepository {

    override suspend fun getCompletedOrders(
    ): List<ReviewOrderDto> = emptyList()

    override suspend fun getOrderForReview(
        orderId: String
    ): ReviewOrderDto? = null

    override suspend fun getReviews(
        serviceId: String
    ): List<Review> = emptyList()

    override suspend fun submitReview(
        orderId: String,
        rating: Int,
        comment: String
    ): Review {
        throw UnsupportedOperationException(
            "Dummy repository tidak terhubung ke database"
        )
    }

    override suspend fun deleteReview(review: Review) {
        // Tidak melakukan operasi database.
    }

    override suspend fun restoreReview(review: Review) {
        // Tidak melakukan operasi database.
    }

    override fun observeOwnReview(
        orderId: String
    ): Flow<Review?> = flowOf(null)
}
