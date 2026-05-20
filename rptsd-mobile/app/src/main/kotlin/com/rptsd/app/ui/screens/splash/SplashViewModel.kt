package com.rptsd.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.repository.AuthRepository
import com.rptsd.app.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Login : SplashDestination()
    object Home : SplashDestination()
    object Subscription : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _destination.value = SplashDestination.Login
                return@launch
            }
            when (val status = subscriptionRepository.getStatus()) {
                is com.rptsd.app.domain.model.Result.Success -> {
                    _destination.value = if (status.data.isActive) {
                        SplashDestination.Home
                    } else {
                        SplashDestination.Subscription
                    }
                }
                is com.rptsd.app.domain.model.Result.Error -> {
                    // Network error — still send to home, let it handle gracefully
                    _destination.value = SplashDestination.Home
                }
            }
        }
    }
}
