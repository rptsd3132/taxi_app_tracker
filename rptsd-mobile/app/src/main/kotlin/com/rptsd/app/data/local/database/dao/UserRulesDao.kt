package com.rptsd.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rptsd.app.data.local.database.entities.UserRulesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRulesDao {

    @Query("SELECT * FROM user_rules WHERE id = 1")
    fun getRulesFlow(): Flow<UserRulesEntity?>

    @Query("SELECT * FROM user_rules WHERE id = 1")
    suspend fun getRules(): UserRulesEntity?

    @Upsert
    suspend fun upsert(rules: UserRulesEntity)

    @Query("UPDATE user_rules SET isAutoAcceptEnabled = :enabled, updatedAt = :ts WHERE id = 1")
    suspend fun updateAutoAcceptEnabled(enabled: Boolean, ts: Long = System.currentTimeMillis())
}
