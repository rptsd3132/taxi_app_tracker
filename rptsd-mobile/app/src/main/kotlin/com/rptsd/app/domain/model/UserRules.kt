package com.rptsd.app.domain.model

data class UserRules(
    val minPrice: Double = 300.0,
    val maxPickupDistance: Double = 5.0,
    val workingHoursStart: String = "08:00",
    val workingHoursEnd: String = "22:00",
    val targetApp: String = "PICKME",
    val randomSkipPercent: Int = 10,
    val isAutoAcceptEnabled: Boolean = false,
    val updatedAt: Long = 0L,
)
