package com.rptsd.app.data.remote.dto.subscription

import com.google.gson.annotations.SerializedName

data class CreatePaymentRequest(
    @SerializedName("amount") val amount: Int,
    @SerializedName("paymentMethod") val paymentMethod: String,
)
