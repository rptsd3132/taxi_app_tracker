package com.rptsd.app.data.remote.dto.subscription

import com.google.gson.annotations.SerializedName

data class ConfirmPaymentRequest(
    @SerializedName("transactionId") val transactionId: String,
)
