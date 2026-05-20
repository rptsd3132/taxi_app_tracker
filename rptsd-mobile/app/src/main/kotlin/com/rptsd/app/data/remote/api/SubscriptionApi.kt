package com.rptsd.app.data.remote.api

import com.rptsd.app.data.remote.dto.auth.ApiResponse
import com.rptsd.app.data.remote.dto.subscription.ConfirmPaymentRequest
import com.rptsd.app.data.remote.dto.subscription.CreatePaymentRequest
import com.rptsd.app.data.remote.dto.subscription.CreatePaymentResponseData
import com.rptsd.app.data.remote.dto.subscription.SubscriptionStatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SubscriptionApi {
    @GET("subscription/status")
    suspend fun getStatus(): ApiResponse<SubscriptionStatusDto>

    @POST("subscription/create-payment")
    suspend fun createPayment(@Body request: CreatePaymentRequest): ApiResponse<CreatePaymentResponseData>

    @POST("subscription/confirm-payment")
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequest): ApiResponse<Map<String, String>>
}
