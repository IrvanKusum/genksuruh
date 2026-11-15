package com.example.genksuruh.feature.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Review(
    val id: String = "",

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("service_id")
    val serviceId: String = "",

    @SerialName("order_id")
    val orderId: String? = null,

    val name: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val date: String = "",

    @SerialName("is_own")
    val isOwn: Boolean = false,

    // Timestamp asli dari Supabase untuk kebutuhan restore.
    val createdAtIso: String = ""
)


enum class DeleteState { Idle, Confirm, Loading, Error }

data class UiMessage(
    val text: String,
    val actionLabel: String? = null,
)

fun ratingLabel(rating: Int): String = when (rating) {
    1 -> "Buruk"
    2 -> "Kurang"
    3 -> "Cukup"
    4 -> "Bagus"
    5 -> "Sangat Bagus"
    else -> ""
}
