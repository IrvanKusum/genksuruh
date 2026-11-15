package com.example.genksuruh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Service(
    val id: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String,
    val areas: List<String> = emptyList(),
    val hours: String = "",
    @SerialName("provider_id")
    val providerId: String = "",
    @SerialName("provider_name")
    val providerName: String = "",
    val rating: Double = 0.0,
    @SerialName("review_count")
    val reviewCount: Int = 0,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @Transient
    val imageRes: Int? = null,
    @Transient
    val isFavorite: Boolean = false
)
