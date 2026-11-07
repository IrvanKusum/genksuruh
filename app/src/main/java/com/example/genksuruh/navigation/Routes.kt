package com.example.genksuruh.navigation

object Routes {
    // Katalog (Irvan)
    const val CATALOG_LIST = "catalog_list"
    const val CATALOG_DETAIL = "catalog_detail/{serviceId}"
    fun catalogDetail(serviceId: String) = "catalog_detail/$serviceId"
    const val CART_LIST = "cart_list"
    const val TRACKING_LIST = "tracking_list"
    const val REVIEW_LIST = "review_list"
    const val FAVORITE_LIST = "favorite_list"
    const val PROMO_LIST = "promo_list"
    const val CATALOG_FORM = "catalog_form?serviceId={serviceId}"
    fun catalogForm(serviceId: String? = null) =
        if (serviceId == null) "catalog_form" else "catalog_form?serviceId=$serviceId"
}