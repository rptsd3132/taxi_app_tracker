package com.rptsd.app.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.repository.SubscriptionRepository
import com.rptsd.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val transactionId: String? = null,
    val paymentConfirmed: Boolean = false,
    val processingPayment: Boolean = false,
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState

    fun pay(amount: Int = 1500, paymentMethod: String = "CARD") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val createResult = subscriptionRepository.createPayment(amount, paymentMethod)) {
                is Result.Success -> {
                    val txId = createResult.data.transactionId
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        processingPayment = true,
                        transactionId = txId,
                    )
                    delay(2000)
                    when (val confirmResult = subscriptionRepository.confirmPayment(txId)) {
                        is Result.Success -> _uiState.value = _uiState.value.copy(
                            processingPayment = false,
                            paymentConfirmed = true,
                        )
                        is Result.Error -> _uiState.value = _uiState.value.copy(
                            processingPayment = false,
                            error = confirmResult.message,
                        )
                    }
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = createResult.message,
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
