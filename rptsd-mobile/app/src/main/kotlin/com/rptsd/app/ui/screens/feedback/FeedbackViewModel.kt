package com.rptsd.app.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.repository.Comment
import com.rptsd.app.data.repository.CommentRepository
import com.rptsd.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val sendSuccess: Boolean = false,
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState

    init { loadComments() }

    fun loadComments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = commentRepository.getMyComments()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    comments = result.data,
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
            }
        }
    }

    fun sendComment(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, error = null, sendSuccess = false)
            when (val result = commentRepository.sendComment(message.trim())) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isSending = false, sendSuccess = true)
                    loadComments()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = result.message,
                )
            }
        }
    }

    fun clearSendSuccess() {
        _uiState.value = _uiState.value.copy(sendSuccess = false)
    }
}
