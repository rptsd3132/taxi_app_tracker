package com.rptsd.app.utils

object Constants {
    const val BASE_URL = "http://10.0.2.2:3001/api/"
    const val DATASTORE_NAME = "rptsd_prefs"
    const val DATABASE_NAME = "rptsd_db"

    // Maps the driver's chosen target app name to the real package name
    fun packageForApp(targetApp: String): String = when (targetApp.uppercase()) {
        "UBER" -> "com.ubercab.driver"
        "BOLT" -> "ee.mtakso.driver"
        else   -> "com.pickme.driver"   // PICKME default
    }
}
