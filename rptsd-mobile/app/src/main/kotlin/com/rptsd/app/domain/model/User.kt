package com.rptsd.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val subscriptionStatus: String,
    val subscriptionEndDate: String?,
) {
    val isSubscriptionActive: Boolean
        get() = subscriptionStatus == "ACTIVE"
}
