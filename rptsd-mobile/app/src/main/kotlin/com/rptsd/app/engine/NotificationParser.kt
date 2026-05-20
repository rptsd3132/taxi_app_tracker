package com.rptsd.app.engine

import com.rptsd.app.domain.model.RideData
import com.rptsd.app.engine.parsers.BoltParser
import com.rptsd.app.engine.parsers.PickMeParser
import com.rptsd.app.engine.parsers.RideNotificationParser
import com.rptsd.app.engine.parsers.UberParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationParser @Inject constructor() {

    private val parsers: List<RideNotificationParser> = listOf(
        PickMeParser(),
        UberParser(),
        BoltParser(),
    )

    fun parse(packageName: String, title: String, text: String): RideData? {
        val parser = parsers.firstOrNull { it.canParse(packageName) } ?: return null
        return parser.parse(title, text)
    }
}
