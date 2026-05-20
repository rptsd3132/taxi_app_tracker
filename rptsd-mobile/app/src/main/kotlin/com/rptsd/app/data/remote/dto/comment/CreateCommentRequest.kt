package com.rptsd.app.data.remote.dto.comment

import com.google.gson.annotations.SerializedName

data class CreateCommentRequest(
    @SerializedName("message") val message: String,
)
