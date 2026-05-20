package com.rptsd.app.data.remote.dto.subscription

import com.google.gson.annotations.SerializedName

data class SubscriptionStatusDto(
    @SerializedName("status") val status: String,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("daysRemaining") val daysRemaining: Int,
    @SerializedName("isActive") val isActive: Boolean,
)
