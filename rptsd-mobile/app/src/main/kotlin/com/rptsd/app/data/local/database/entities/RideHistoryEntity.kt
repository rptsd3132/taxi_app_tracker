package com.rptsd.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_history")
data class RideHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val detectedAt: Long,
    val price: Double,
    val pickupDistance: Double,
    val pickupLocation: String,
    val decision: String,
    val reason: String,
    val rawNotification: String,
    val sourceApp: String,
    val isSynced: Boolean = false,
)
