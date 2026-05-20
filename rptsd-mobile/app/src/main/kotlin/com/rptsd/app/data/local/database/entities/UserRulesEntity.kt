package com.rptsd.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_rules")
data class UserRulesEntity(
    @PrimaryKey val id: Int = 1,
    val minPrice: Double,
    val maxPickupDistance: Double,
    val workingHoursStart: String,
    val workingHoursEnd: String,
    val targetApp: String,
    val randomSkipPercent: Int,
    val isAutoAcceptEnabled: Boolean,
    val updatedAt: Long,
)
