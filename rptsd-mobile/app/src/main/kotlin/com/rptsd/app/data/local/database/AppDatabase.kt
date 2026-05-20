package com.rptsd.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rptsd.app.data.local.database.dao.RideHistoryDao
import com.rptsd.app.data.local.database.dao.UserRulesDao
import com.rptsd.app.data.local.database.entities.RideHistoryEntity
import com.rptsd.app.data.local.database.entities.UserRulesEntity

@Database(
    entities = [UserRulesEntity::class, RideHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userRulesDao(): UserRulesDao
    abstract fun rideHistoryDao(): RideHistoryDao
}
