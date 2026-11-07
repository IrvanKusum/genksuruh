package com.example.genksuruh.feature.review

data class Review(
    val id: Int,
    val name: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val isOwn: Boolean = false,
    val orderId: String? = null,
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