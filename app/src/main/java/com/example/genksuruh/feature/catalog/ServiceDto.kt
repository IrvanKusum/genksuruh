package com.example.genksuruh.feature.catalog

import com.example.genksuruh.core.model.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Baris tabel `services` (untuk membaca). */
@Serializable
data class ServiceRow(
    val id: String,
    @SerialName("provider_id") val providerId: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String = "",
    val areas: List<String> = emptyList(),
    val hours: String = "",
    @SerialName("provider_name") val providerName: String = "",
    @SerialName("provider_phone") val providerPhone: String = "085895089845",
    @SerialName("image_url") val imageUrl: String? = null,
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0
)

/** Dikirim saat TAMBAH. Tanpa rating dan review_count (dihitung trigger). */
@Serializable
data class ServiceInsert(
    @SerialName("provider_id") val providerId: String,
    @SerialName("provider_name") val providerName: String,
    @SerialName("provider_phone") val providerPhone: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String,
    val areas: List<String>,
    val hours: String,
    @SerialName("image_url") val imageUrl: String?
)

/** Dikirim saat EDIT. Pemilik jasa tidak diubah. */
@Serializable
data class ServiceUpdate(
    @SerialName("provider_name") val providerName: String,
    @SerialName("provider_phone") val providerPhone: String,
    val title: String,
    val category: String,
    val price: Long,
    val description: String,
    val areas: List<String>,
    val hours: String,
    @SerialName("image_url") val imageUrl: String?
)

@Serializable
data class ProfileName(val id: String, val name: String)

fun ServiceRow.toService(defaultProviderName: String) = Service(
    id = id,
    title = title,
    category = category,
    price = price,
    description = description,
    areas = areas,
    hours = hours,
    providerId = providerId,
    providerName = if (providerName.isNotBlank()) providerName else defaultProviderName,
    providerPhone = providerPhone.ifBlank { "085895089845" },
    rating = rating,
    reviewCount = reviewCount,
    imageRes = null,
    imageUrl = imageUrl,
    isFavorite = false
)
