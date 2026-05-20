package com.rptsd.app.data.repository

import com.rptsd.app.data.remote.api.CommentApi
import com.rptsd.app.data.remote.dto.comment.CommentDto
import com.rptsd.app.data.remote.dto.comment.CreateCommentRequest
import com.rptsd.app.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

data class Comment(
    val id: String,
    val message: String,
    val adminReply: String?,
    val repliedAt: String?,
    val repliedBy: String?,
    val status: String,
    val createdAt: String,
)

@Singleton
class CommentRepository @Inject constructor(
    private val commentApi: CommentApi,
) {
    suspend fun sendComment(message: String): Result<Unit> {
        return try {
            val response = commentApi.create(CreateCommentRequest(message))
            if (response.success) Result.Success(Unit)
            else Result.Error(response.error ?: "Failed to send feedback")
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }

    suspend fun getMyComments(): Result<List<Comment>> {
        return try {
            val response = commentApi.getMy()
            if (response.success && response.data != null) {
                Result.Success(response.data.map { it.toModel() })
            } else {
                Result.Error(response.error ?: "Failed to load comments")
            }
        } catch (e: Exception) {
            Result.Error(e.toReadableMessage())
        }
    }
}

private fun CommentDto.toModel() = Comment(
    id = id,
    message = message,
    adminReply = adminReply,
    repliedAt = repliedAt,
    repliedBy = repliedBy,
    status = status,
    createdAt = createdAt,
)

private fun Exception.toReadableMessage(): String = when {
    message?.contains("Unable to resolve host") == true ||
    message?.contains("connect") == true -> "No internet connection"
    else -> message ?: "Something went wrong"
}
