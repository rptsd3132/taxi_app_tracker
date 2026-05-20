package com.rptsd.app.data.repository

import com.rptsd.app.data.local.database.dao.RideHistoryDao
import com.rptsd.app.data.local.database.dao.TodayStats
import com.rptsd.app.data.local.database.entities.RideHistoryEntity
import com.rptsd.app.domain.model.Decision
import com.rptsd.app.domain.model.RideData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class RideHistory(
    val id: Long,
    val detectedAt: Long,
    val price: Double,
    val pickupDistance: Double,
    val pickupLocation: String,
    val decision: String,
    val reason: String,
    val sourceApp: String,
    val isSynced: Boolean,
)

@Singleton
class RideHistoryRepository @Inject constructor(
    private val rideHistoryDao: RideHistoryDao,
) {
    fun observeRecent(limit: Int): Flow<List<RideHistory>> =
        rideHistoryDao.getRecent(limit).map { list -> list.map { it.toModel() } }

    suspend fun saveRide(ride: RideData, decision: Decision): Long {
        val entity = RideHistoryEntity(
            detectedAt = ride.detectedAt,
            price = ride.price,
            pickupDistance = ride.pickupDistance,
            pickupLocation = ride.pickupLocation,
            decision = when (decision) {
                is Decision.Accept -> "ACCEPTED"
                is Decision.Skip -> "SKIPPED"
            },
            reason = when (decision) {
                is Decision.Accept -> "Rules matched"
                is Decision.Skip -> decision.reason
            },
            rawNotification = ride.rawText,
            sourceApp = ride.sourceApp,
        )
        return rideHistoryDao.insert(entity)
    }

    suspend fun getTodayStats(): TodayStats {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return rideHistoryDao.getTodayStats(startOfDay)
    }

    suspend fun getStatsBetween(startMs: Long, endMs: Long): TodayStats =
        rideHistoryDao.getStatsBetween(startMs, endMs)

    suspend fun getRidesByDate(startMs: Long, endMs: Long): List<RideHistory> =
        rideHistoryDao.getByDate(startMs, endMs).map { it.toModel() }

    suspend fun getUnsynced(): List<RideHistory> =
        rideHistoryDao.getUnsynced().map { it.toModel() }

    suspend fun markSynced(ids: List<Long>) =
        rideHistoryDao.markSynced(ids)
}

private fun RideHistoryEntity.toModel() = RideHistory(
    id = id,
    detectedAt = detectedAt,
    price = price,
    pickupDistance = pickupDistance,
    pickupLocation = pickupLocation,
    decision = decision,
    reason = reason,
    sourceApp = sourceApp,
    isSynced = isSynced,
)
