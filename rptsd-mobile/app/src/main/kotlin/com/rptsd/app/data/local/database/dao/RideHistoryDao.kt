package com.rptsd.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rptsd.app.data.local.database.entities.RideHistoryEntity
import kotlinx.coroutines.flow.Flow

data class TodayStats(
    val totalRides: Int,
    val accepted: Int,
    val skipped: Int,
    val missed: Int,
    val totalEarnings: Double,
)

@Dao
interface RideHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ride: RideHistoryEntity): Long

    @Query("SELECT * FROM ride_history ORDER BY detectedAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<RideHistoryEntity>>

    @Query("SELECT * FROM ride_history WHERE detectedAt >= :startMs AND detectedAt <= :endMs ORDER BY detectedAt DESC")
    suspend fun getByDate(startMs: Long, endMs: Long): List<RideHistoryEntity>

    @Query("SELECT * FROM ride_history WHERE isSynced = 0 ORDER BY detectedAt ASC")
    suspend fun getUnsynced(): List<RideHistoryEntity>

    @Query("UPDATE ride_history SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("""
        SELECT
            COUNT(*) AS totalRides,
            SUM(CASE WHEN decision = 'ACCEPTED' THEN 1 ELSE 0 END) AS accepted,
            SUM(CASE WHEN decision = 'SKIPPED' THEN 1 ELSE 0 END) AS skipped,
            SUM(CASE WHEN decision = 'MISSED' THEN 1 ELSE 0 END) AS missed,
            IFNULL(SUM(CASE WHEN decision = 'ACCEPTED' THEN price ELSE 0 END), 0.0) AS totalEarnings
        FROM ride_history
        WHERE detectedAt >= :startOfDayMs
    """)
    suspend fun getTodayStats(startOfDayMs: Long): TodayStats

    @Query("""
        SELECT
            COUNT(*) AS totalRides,
            SUM(CASE WHEN decision = 'ACCEPTED' THEN 1 ELSE 0 END) AS accepted,
            SUM(CASE WHEN decision = 'SKIPPED' THEN 1 ELSE 0 END) AS skipped,
            SUM(CASE WHEN decision = 'MISSED' THEN 1 ELSE 0 END) AS missed,
            IFNULL(SUM(CASE WHEN decision = 'ACCEPTED' THEN price ELSE 0 END), 0.0) AS totalEarnings
        FROM ride_history
        WHERE detectedAt >= :startMs AND detectedAt <= :endMs
    """)
    suspend fun getStatsBetween(startMs: Long, endMs: Long): TodayStats
}
