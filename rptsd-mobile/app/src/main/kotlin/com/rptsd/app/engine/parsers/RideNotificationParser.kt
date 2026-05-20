package com.rptsd.app.engine.parsers

import com.rptsd.app.domain.model.RideData

interface RideNotificationParser {
    fun canParse(packageName: String): Boolean
    fun parse(title: String, text: String): RideData?
}
