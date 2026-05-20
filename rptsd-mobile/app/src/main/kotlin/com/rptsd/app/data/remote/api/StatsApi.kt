package com.rptsd.app.data.remote.api

import com.rptsd.app.data.remote.dto.auth.ApiResponse
import com.rptsd.app.data.remote.dto.stats.StatsDto
import com.rptsd.app.data.remote.dto.stats.SyncStatsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StatsApi {
    @POST("stats/sync")
    suspend fun sync(@Body request: SyncStatsRequest): ApiResponse<Unit>

    @GET("stats/my")
    suspend fun getMyStats(@Query("days") days: Int): ApiResponse<List<StatsDto>>
}
