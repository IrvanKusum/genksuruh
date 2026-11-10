package com.example.genksuruh.core.model

data class Service(
    val id: String,
    val title: String,
    val category: String,
    val price: Int,
    val description: String,
    val areas: List<String>,
    val hours: String,
    val providerId: String,
    val providerName: String,
    val rating: Double,
    val reviewCount: Int,
    val imageRes: Int? = null,      // dummy (Progress 1)
    val imageUrl: String? = null,   // Supabase Storage (Progress 2)
    val isFavorite: Boolean = false
)