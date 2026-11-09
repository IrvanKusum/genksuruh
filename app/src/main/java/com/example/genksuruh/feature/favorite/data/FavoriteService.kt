package com.example.genksuruh.feature.favorite.data

data class FavoriteService(
    val id: String,
    val title: String,
    val providerName: String,
    val providerInfo: String,
    val category: String,
    val price: Int,
    val originalPrice: Int? = null,
    val discountLabel: String? = null,
    val rating: Double,
    val reviewCount: Int,
    val bannerLabel: String,
    val area: String,
    val estimate: String,
    val description: String
)
