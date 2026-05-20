package com.rptsd.app.data.remote.api

import com.rptsd.app.data.remote.dto.auth.ApiResponse
import com.rptsd.app.data.remote.dto.auth.LoginRequest
import com.rptsd.app.data.remote.dto.auth.LoginResponseData
import com.rptsd.app.data.remote.dto.auth.RegisterRequest
import com.rptsd.app.data.remote.dto.auth.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<LoginResponseData>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponseData>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @GET("auth/me")
    suspend fun me(): ApiResponse<UserDto>
}
