package com.rptsd.app.engine

import com.rptsd.app.domain.model.Decision
import com.rptsd.app.domain.model.RideData
import com.rptsd.app.domain.model.UserRules
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun evaluate(ride: RideData, rules: UserRules): Decision {
        if (!rules.isAutoAcceptEnabled)
            return Decision.Skip("Auto-accept disabled")

        if (!isWithinWorkingHours(rules.workingHoursStart, rules.workingHoursEnd))
            return Decision.Skip("Outside working hours")

        if (ride.price < rules.minPrice)
            return Decision.Skip("Price too low (${ride.price} < ${rules.minPrice})")

        if (ride.pickupDistance > rules.maxPickupDistance)
            return Decision.Skip("Too far (${ride.pickupDistance} km > ${rules.maxPickupDistance} km)")

        if (HumanBehavior.shouldRandomSkip(rules.randomSkipPercent))
            return Decision.Skip("Random skip (humanness)")

        return Decision.Accept
    }

    private fun isWithinWorkingHours(startStr: String, endStr: String): Boolean {
        return try {
            val now = LocalTime.now()
            val start = LocalTime.parse(startStr, timeFormatter)
            val end = LocalTime.parse(endStr, timeFormatter)
            if (start <= end) {
                now >= start && now <= end
            } else {
                // Overnight span (e.g. 22:00–06:00)
                now >= start || now <= end
            }
        } catch (_: Exception) {
            true // Parse failure → don't block rides
        }
    }
}
