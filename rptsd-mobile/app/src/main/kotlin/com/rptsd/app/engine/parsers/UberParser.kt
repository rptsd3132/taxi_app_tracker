package com.rptsd.app.engine.parsers

import com.rptsd.app.domain.model.RideData

class UberParser : RideNotificationParser {

    companion object {
        private val PACKAGES = setOf(
            "com.ubercab.driver",
            "com.ubercab",
        )

        // "Trip request: $4.50 - 5 min (2.1 km)"
        private val PATTERN_DOLLAR = Regex(
            """\$(\d+(?:\.\d+)?)[^0-9]*(\d+(?:\.\d+)?)\s*km""",
            RegexOption.IGNORE_CASE,
        )

        // "New trip – USD 3.20, 1.8 km, Beira Lake"
        private val PATTERN_USD = Regex(
            """USD\s*(\d+(?:\.\d+)?)[,\s]+(\d+(?:\.\d+)?)\s*km(?:[,\s]+(.+))?""",
            RegexOption.IGNORE_CASE,
        )

        // Generic: some amount followed by km
        private val PATTERN_GENERIC = Regex(
            """(\d+(?:\.\d+)?)\s*(?:USD|[\$]).*?(\d+(?:\.\d+)?)\s*km""",
            RegexOption.IGNORE_CASE,
        )
    }

    override fun canParse(packageName: String): Boolean = packageName in PACKAGES

    override fun parse(title: String, text: String): RideData? {
        val combined = "$title $text".trim()

        val match = PATTERN_DOLLAR.find(combined)
            ?: PATTERN_USD.find(combined)
            ?: PATTERN_GENERIC.find(combined)
            ?: return null

        // Convert USD to approximate LKR (rough factor for display; engine uses LKR min)
        val priceRaw = match.groupValues[1].toDoubleOrNull() ?: return null
        val price = priceRaw * 300.0   // 1 USD ≈ 300 LKR
        val distance = match.groupValues[2].toDoubleOrNull() ?: return null
        val location = match.groupValues.getOrNull(3)?.trim().orEmpty()

        return RideData(
            price = price,
            pickupDistance = distance,
            pickupLocation = location,
            sourceApp = "UBER",
            rawText = combined,
        )
    }
}
