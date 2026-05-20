package com.rptsd.app.data.repository

import com.rptsd.app.data.remote.api.StatsApi
import com.rptsd.app.data.remote.dto.stats.SyncStatsRequest
import com.rptsd.app.domain.model.Result
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val statsApi: StatsApi,
    private val rideHistoryRepository: RideHistoryRepository,
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun syncTodayStats(): Result<Unit> {
        return try {
            val stats = rideHistoryRepository.getTodayStats()
            val today = dateFormat.format(Date())
            statsApi.sync(
                SyncStatsRequest(
                    date = today,
                    ridesAccepted = stats.accepted,
                    ridesSkipped = stats.skipped,
                    totalEarnings = stats.totalEarnings,
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sync failed")
        }
    }

    suspend fun syncDateRange(startMs: Long, endMs: Long): Result<Unit> {
        return try {
            val stats = rideHistoryRepository.getStatsBetween(startMs, endMs)
            val date = dateFormat.format(Date(startMs))
            statsApi.sync(
                SyncStatsRequest(
                    date = date,
                    ridesAccepted = stats.accepted,
                    ridesSkipped = stats.skipped,
                    totalEarnings = stats.totalEarnings,
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sync failed")
        }
    }

    private fun startOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
