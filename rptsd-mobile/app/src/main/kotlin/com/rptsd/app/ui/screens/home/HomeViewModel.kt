package com.rptsd.app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.local.database.dao.TodayStats
import com.rptsd.app.data.repository.RideHistory
import com.rptsd.app.data.repository.RideHistoryRepository
import com.rptsd.app.data.repository.RulesRepository
import com.rptsd.app.domain.model.UserRules
import com.rptsd.app.services.ForegroundMonitor
import com.rptsd.app.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val rules: UserRules = UserRules(),
    val todayStats: TodayStats = TodayStats(0, 0, 0, 0, 0.0),
    val recentRides: List<RideHistory> = emptyList(),
    val isNotificationPermissionGranted: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rulesRepository: RulesRepository,
    private val rideHistoryRepository: RideHistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        checkPermission()
        viewModelScope.launch {
            combine(
                rulesRepository.observeRules(),
                rideHistoryRepository.observeRecent(5),
            ) { rules, recent -> rules to recent }
            .collect { (rules, recent) ->
                val stats = rideHistoryRepository.getTodayStats()
                _uiState.value = _uiState.value.copy(
                    rules = rules,
                    todayStats = stats,
                    recentRides = recent,
                )
            }
        }
    }

    fun checkPermission() {
        _uiState.value = _uiState.value.copy(
            isNotificationPermissionGranted = PermissionUtils.isNotificationListenerEnabled(context),
        )
    }

    fun toggleAutoAccept(enabled: Boolean) {
        viewModelScope.launch {
            rulesRepository.toggleAutoAccept(enabled)
            if (enabled) ForegroundMonitor.start(context)
            else ForegroundMonitor.stop(context)
        }
    }
}
