package com.rptsd.app.data.remote.dto.subscription

import com.google.gson.annotations.SerializedName

data class CreatePaymentResponseData(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("paymentUrl") val paymentUrl: String,
)
