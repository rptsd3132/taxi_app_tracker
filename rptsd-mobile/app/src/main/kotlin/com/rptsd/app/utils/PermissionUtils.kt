package com.rptsd.app.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object PermissionUtils {

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        val cn = ComponentName(context, "com.rptsd.app.services.NotificationListener")
        return flat.split(":").any { it.trim() == cn.flattenToString() }
    }

    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedCn = ComponentName(context, serviceClass).flattenToShortString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedCn, ignoreCase = true)) return true
        }
        return false
    }
}
