package com.rptsd.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.repository.RulesRepository
import com.rptsd.app.domain.model.UserRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val rules: UserRules = UserRules(),
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val rulesRepository: RulesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            rulesRepository.observeRules().collect { rules ->
                // Only update from DB if user hasn't made local edits yet
                if (!_uiState.value.isSaving) {
                    _uiState.value = _uiState.value.copy(rules = rules)
                }
            }
        }
    }

    fun updateMinPrice(value: Double) {
        _uiState.value = _uiState.value.copy(rules = _uiState.value.rules.copy(minPrice = value))
    }

    fun updateMaxDistance(value: Double) {
        _uiState.value = _uiState.value.copy(rules = _uiState.value.rules.copy(maxPickupDistance = value))
    }

    fun updateWorkingStart(value: String) {
        _uiState.value = _uiState.value.copy(rules = _uiState.value.rules.copy(workingHoursStart = value))
    }

    fun updateWorkingEnd(value: String) {
        _uiState.value = _uiState.value.copy(rules = _uiState.value.rules.copy(workingHoursEnd = value))
    }

    fun updateTargetApp(value: String) {
        _uiState.value = _uiState.value.copy(rules = _uiState.value.rules.copy(targetApp = value))
    }

    fun updateRandomSkipPercent(value: Int) {
        _uiState.value = _uiState.value.copy(rules = _uiState.value.rules.copy(randomSkipPercent = value))
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, savedSuccess = false)
            rulesRepository.saveRules(_uiState.value.rules)
            _uiState.value = _uiState.value.copy(isSaving = false, savedSuccess = true)
        }
    }

    fun clearSavedSuccess() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }
}
