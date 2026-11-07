package com.example.genksuruh.navigation

object Routes {
    // ===== Katalog (Irvan) =====
    const val CATALOG_LIST = "catalog_list"
    const val CATALOG_DETAIL = "catalog_detail/{serviceId}"
    const val CATALOG_FORM = "catalog_form?serviceId={serviceId}"

    fun catalogDetail(id: String) = "catalog_detail/$id"
    fun catalogForm(serviceId: String? = null) =
        if (serviceId == null) "catalog_form" else "catalog_form?serviceId=$serviceId"

    // ===== Keranjang (Rasyid) =====
    const val CART_LIST = "cart_list"

    // ===== Status & Riwayat (Hilmiy) =====
    const val TRACKING_LIST = "tracking_list"
    const val TRACKING_DETAIL = "tracking_detail/{orderId}"
    const val TRACKING_UPDATE = "tracking_update/{orderId}"

    fun trackingDetail(orderId: String) = "tracking_detail/$orderId"
    fun trackingUpdate(orderId: String) = "tracking_update/$orderId"

    // ===== Ulasan (Ahsana) =====
    const val REVIEW_LIST = "review_list"
    const val REVIEW = "review/{orderId}"

    fun review(orderId: String) = "review/$orderId"

    // ===== Lainnya =====
    const val FAVORITE_LIST = "favorite_list"
    const val PROMO_LIST = "promo_list"
}