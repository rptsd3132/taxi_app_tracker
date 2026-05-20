package com.rptsd.app.data.remote.api

import com.rptsd.app.data.remote.dto.auth.ApiResponse
import com.rptsd.app.data.remote.dto.comment.CommentDto
import com.rptsd.app.data.remote.dto.comment.CreateCommentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CommentApi {
    @POST("comments")
    suspend fun create(@Body request: CreateCommentRequest): ApiResponse<CommentDto>

    @GET("comments/my")
    suspend fun getMy(): ApiResponse<List<CommentDto>>
}
