package com.rptsd.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("subscriptionStatus") val subscriptionStatus: String,
    @SerializedName("subscriptionEndDate") val subscriptionEndDate: String?,
)
