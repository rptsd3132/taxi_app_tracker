package com.rptsd.app.data.remote.dto.comment

import com.google.gson.annotations.SerializedName

data class CommentDto(
    @SerializedName("id") val id: String,
    @SerializedName("message") val message: String,
    @SerializedName("adminReply") val adminReply: String?,
    @SerializedName("repliedAt") val repliedAt: String?,
    @SerializedName("repliedBy") val repliedBy: String?,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
)
