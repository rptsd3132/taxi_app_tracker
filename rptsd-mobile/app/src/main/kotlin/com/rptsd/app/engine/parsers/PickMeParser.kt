package com.rptsd.app.engine.parsers

import com.rptsd.app.domain.model.RideData

class PickMeParser : RideNotificationParser {

    companion object {
        private val PACKAGES = setOf(
            "com.pickme.passenger",
            "com.pickme.driver",
            "lk.gov.pickme",
        )

        // "Rs.450 - Pickup 2.3km away from Colombo 03"
        private val PATTERN_RS_PICKUP = Regex(
            """(?:Rs\.?|LKR)\s*(\d+(?:\.\d+)?)\s*[-–|]\s*(?:Pickup\s+)?(\d+(?:\.\d+)?)\s*km(?:\s+away)?(?:\s+from\s+(.+?))?$""",
            RegexOption.IGNORE_CASE,
        )

        // "New trip: LKR 320, 1.5 km pickup, Nugegoda"
        private val PATTERN_NEW_TRIP = Regex(
            """(?:New\s+(?:trip|ride)(?:\s+request)?[:\s]+)?(?:Rs\.?|LKR)\s*(\d+(?:\.\d+)?)[,\s]+(\d+(?:\.\d+)?)\s*km(?:[,\s]+(.+))?""",
            RegexOption.IGNORE_CASE,
        )

        // "Rs.1250 | 3.2km | Kandy"
        private val PATTERN_PIPE = Regex(
            """(?:Rs\.?|LKR)\s*(\d+(?:\.\d+)?)\s*\|\s*(\d+(?:\.\d+)?)\s*km\s*\|\s*(.+)""",
            RegexOption.IGNORE_CASE,
        )
    }

    override fun canParse(packageName: String): Boolean = packageName in PACKAGES

    override fun parse(title: String, text: String): RideData? {
        val combined = "$title $text".trim()

        val match = PATTERN_RS_PICKUP.find(combined)
            ?: PATTERN_NEW_TRIP.find(combined)
            ?: PATTERN_PIPE.find(combined)
            ?: return null

        val price = match.groupValues[1].toDoubleOrNull() ?: return null
        val distance = match.groupValues[2].toDoubleOrNull() ?: return null
        val location = match.groupValues.getOrNull(3)?.trim().orEmpty()

        return RideData(
            price = price,
            pickupDistance = distance,
            pickupLocation = location,
            sourceApp = "PICKME",
            rawText = combined,
        )
    }
}
