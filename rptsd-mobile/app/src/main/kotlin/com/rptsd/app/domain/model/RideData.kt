package com.rptsd.app.domain.model

data class RideData(
    val price: Double,
    val pickupDistance: Double,
    val pickupLocation: String,
    val sourceApp: String,
    val rawText: String,
    val detectedAt: Long = System.currentTimeMillis(),
)
