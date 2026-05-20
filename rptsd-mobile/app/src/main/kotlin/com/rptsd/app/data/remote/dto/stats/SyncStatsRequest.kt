package com.rptsd.app.data.remote.dto.stats

import com.google.gson.annotations.SerializedName

data class SyncStatsRequest(
    @SerializedName("date") val date: String,
    @SerializedName("ridesAccepted") val ridesAccepted: Int,
    @SerializedName("ridesSkipped") val ridesSkipped: Int,
    @SerializedName("totalEarnings") val totalEarnings: Double,
)
