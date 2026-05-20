package com.rptsd.app.engine.parsers

import com.rptsd.app.domain.model.RideData

class BoltParser : RideNotificationParser {

    companion object {
        private val PACKAGES = setOf(
            "ee.mtakso.driver",
            "ee.mtakso.client",
        )

        // "New order €5.50 - 2.3 km, Old Town"
        private val PATTERN_EURO = Regex(
            """(?:New\s+order\s+)?[€£](\d+(?:\.\d+)?)[\s\-–]+(\d+(?:\.\d+)?)\s*km(?:[,\s]+(.+))?""",
            RegexOption.IGNORE_CASE,
        )

        // "Ride request: 500 LKR, 3.1 km away"
        private val PATTERN_LKR = Regex(
            """(\d+(?:\.\d+)?)\s*(?:LKR|Rs\.?)[,\s]+(\d+(?:\.\d+)?)\s*km(?:\s+away)?(?:[,\s]+(.+))?""",
            RegexOption.IGNORE_CASE,
        )

        // Generic Bolt fallback: extract any number as currency + km
        private val PATTERN_GENERIC = Regex(
            """(\d+(?:\.\d+)?)\s*(?:€|£|USD|EUR)[^0-9]*(\d+(?:\.\d+)?)\s*km""",
            RegexOption.IGNORE_CASE,
        )
    }

    override fun canParse(packageName: String): Boolean = packageName in PACKAGES

    override fun parse(title: String, text: String): RideData? {
        val combined = "$title $text".trim()

        val match = PATTERN_LKR.find(combined)
            ?: PATTERN_EURO.find(combined)
            ?: PATTERN_GENERIC.find(combined)
            ?: return null

        val priceRaw = match.groupValues[1].toDoubleOrNull() ?: return null
        // Normalise EUR → LKR if the amount looks like a Euro price (< 100)
        val price = if (priceRaw < 100 && combined.contains(Regex("[€£]"))) priceRaw * 350 else priceRaw
        val distance = match.groupValues[2].toDoubleOrNull() ?: return null
        val location = match.groupValues.getOrNull(3)?.trim().orEmpty()

        return RideData(
            price = price,
            pickupDistance = distance,
            pickupLocation = location,
            sourceApp = "BOLT",
            rawText = combined,
        )
    }
}
