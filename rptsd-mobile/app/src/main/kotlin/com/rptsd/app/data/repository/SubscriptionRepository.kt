package com.rptsd.app.data.repository

import com.rptsd.app.data.remote.api.SubscriptionApi
import com.rptsd.app.data.remote.dto.subscription.ConfirmPaymentRequest
import com.rptsd.app.data.remote.dto.subscription.CreatePaymentRequest
import com.rptsd.app.data.remote.dto.subscription.CreatePaymentResponseData
import com.rptsd.app.data.remote.dto.subscription.SubscriptionStatusDto
import com.rptsd.app.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionApi: SubscriptionApi,
) {
    suspend fun getStatus(): Result<SubscriptionStatusDto> {
        return try {
            val response = subscriptionApi.getStatus()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to fetch subscription status")
            }
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }

    suspend fun createPayment(amount: Int, paymentMethod: String): Result<CreatePaymentResponseData> {
        return try {
            val response = subscriptionApi.createPayment(CreatePaymentRequest(amount, paymentMethod))
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to create payment")
            }
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }

    suspend fun confirmPayment(transactionId: String): Result<String> {
        return try {
            val response = subscriptionApi.confirmPayment(ConfirmPaymentRequest(transactionId))
            if (response.success) {
                Result.Success(response.data?.get("subscriptionEndDate") ?: "")
            } else {
                Result.Error(response.error ?: "Payment confirmation failed")
            }
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }
}

private fun Exception.toReadableMessage(): String = when {
    message?.contains("Unable to resolve host") == true ||
    message?.contains("connect") == true -> "No internet connection"
    message?.contains("timeout") == true -> "Request timed out"
    else -> message ?: "Something went wrong"
}
