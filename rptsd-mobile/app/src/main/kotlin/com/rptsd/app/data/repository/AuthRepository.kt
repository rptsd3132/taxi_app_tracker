package com.rptsd.app.data.repository

import com.rptsd.app.data.local.datastore.TokenManager
import com.rptsd.app.data.remote.api.AuthApi
import com.rptsd.app.data.remote.dto.auth.LoginRequest
import com.rptsd.app.data.remote.dto.auth.RegisterRequest
import com.rptsd.app.domain.model.Result
import com.rptsd.app.domain.model.User
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
) {
    suspend fun login(email: String, password: String, deviceId: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password, deviceId))
            if (response.success && response.data != null) {
                tokenManager.saveToken(response.data.token)
                Result.Success(response.data.user.toDomain())
            } else {
                Result.Error(response.error ?: response.message ?: "Login failed")
            }
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(name, email, phone, password))
            if (response.success && response.data != null) {
                tokenManager.saveToken(response.data.token)
                Result.Success(response.data.user.toDomain())
            } else {
                Result.Error(response.error ?: response.message ?: "Registration failed")
            }
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }

    suspend fun logout() {
        try { authApi.logout() } catch (_: Exception) { }
        tokenManager.clearToken()
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.token.firstOrNull() != null

    suspend fun getCurrentUser(): User? {
        return try {
            val response = authApi.me()
            if (response.success && response.data != null) response.data.toDomain() else null
        } catch (_: Exception) {
            null
        }
    }
}

private fun com.rptsd.app.data.remote.dto.auth.UserDto.toDomain() = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    subscriptionStatus = subscriptionStatus,
    subscriptionEndDate = subscriptionEndDate,
)

private fun Exception.toReadableMessage(): String = when {
    message?.contains("Unable to resolve host") == true ||
    message?.contains("connect") == true -> "No internet connection"
    message?.contains("timeout") == true -> "Request timed out"
    else -> message ?: "Something went wrong"
}
