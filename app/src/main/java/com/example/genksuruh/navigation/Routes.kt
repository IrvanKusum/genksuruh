package com.example.genksuruh.navigation

object Routes {
    const val CATALOG_LIST = "catalog_list"
    const val CART_LIST = "cart_list"
    const val TRACKING_LIST = "tracking_list"
    const val REVIEW_LIST = "review_list"
    const val FAVORITE_LIST = "favorite_list"
    const val PROMO_LIST = "promo_list"

    //Hilmiy
    const val TRACKING_DETAIL = "tracking_detail/{orderId}"
    const val TRACKING_UPDATE = "tracking_update/{orderId}"
    fun trackingDetail(id: String) = "tracking_detail/$id"
    fun trackingUpdate(id: String) = "tracking_update/$id"
}