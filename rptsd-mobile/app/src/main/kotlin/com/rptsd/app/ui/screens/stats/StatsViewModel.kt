package com.rptsd.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.local.database.dao.TodayStats
import com.rptsd.app.data.repository.RideHistory
import com.rptsd.app.data.repository.RideHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class DateRange { TODAY, WEEK, MONTH }

data class DayBar(
    val label: String,
    val accepted: Int,
    val skipped: Int,
    val earnings: Double,
)

data class StatsUiState(
    val selectedRange: DateRange = DateRange.TODAY,
    val summary: TodayStats = TodayStats(0, 0, 0, 0, 0.0),
    val history: List<RideHistory> = emptyList(),
    val barData: List<DayBar> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val rideHistoryRepository: RideHistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        selectRange(DateRange.TODAY)
    }

    fun selectRange(range: DateRange) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedRange = range)
            val (start, end) = rangeMs(range)
            val summary = rideHistoryRepository.getStatsBetween(start, end)
            val history = rideHistoryRepository.getRidesByDate(start, end)
            val barData = buildBarData(range)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                summary = summary,
                history = history,
                barData = barData,
            )
        }
    }

    private suspend fun buildBarData(range: DateRange): List<DayBar> {
        val days = when (range) {
            DateRange.TODAY -> 1
            DateRange.WEEK -> 7
            DateRange.MONTH -> 30
        }
        val cal = Calendar.getInstance()
        val bars = mutableListOf<DayBar>()
        repeat(days) { daysAgo ->
            cal.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + 86_400_000L - 1
            val stats = rideHistoryRepository.getStatsBetween(dayStart, dayEnd)
            val label = if (days == 1) "Today" else dayLabel(cal, daysAgo, days)
            bars.add(0, DayBar(label, stats.accepted, stats.skipped, stats.totalEarnings))
        }
        return bars
    }

    private fun dayLabel(cal: Calendar, daysAgo: Int, totalDays: Int): String {
        return if (totalDays <= 7) {
            arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")[cal.get(Calendar.DAY_OF_WEEK) - 1]
        } else {
            "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
        }
    }

    private fun rangeMs(range: DateRange): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = System.currentTimeMillis()
        return when (range) {
            DateRange.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to end
            }
            DateRange.WEEK -> end - 7 * 86_400_000L to end
            DateRange.MONTH -> end - 30 * 86_400_000L to end
        }
    }
}
