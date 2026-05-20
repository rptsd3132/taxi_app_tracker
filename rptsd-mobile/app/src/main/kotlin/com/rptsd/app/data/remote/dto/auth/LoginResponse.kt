package com.rptsd.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class LoginResponseData(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String,
)
