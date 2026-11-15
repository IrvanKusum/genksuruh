
package com.example.genksuruh.feature.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewOrderDto(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("service_id")
    val serviceId: String,

    @SerialName("service_name")
    val serviceName: String? = null,

    @SerialName("package_name")
    val packageName: String? = null,

    @SerialName("schedule_text")
    val scheduleText: String? = null,

    @SerialName("total_price")
    val totalPrice: Long? = null,

    val status: String
)
