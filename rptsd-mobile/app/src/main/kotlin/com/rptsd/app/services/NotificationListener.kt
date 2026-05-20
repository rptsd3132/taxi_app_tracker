package com.rptsd.app.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.rptsd.app.data.repository.RideHistoryRepository
import com.rptsd.app.data.repository.RulesRepository
import com.rptsd.app.domain.model.Decision
import com.rptsd.app.engine.NotificationParser
import com.rptsd.app.engine.RuleEngine
import com.rptsd.app.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject lateinit var parser: NotificationParser
    @Inject lateinit var ruleEngine: RuleEngine
    @Inject lateinit var rulesRepository: RulesRepository
    @Inject lateinit var historyRepository: RideHistoryRepository
    @Inject lateinit var acceptanceQueue: AcceptanceQueue

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        serviceScope.launch {
            processNotification(sbn)
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
        val rules = rulesRepository.observeRules().first()

        // Only process notifications from the driver's target app
        if (sbn.packageName != Constants.packageForApp(rules.targetApp)) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_TEXT) ?: "").toString()

        val ride = parser.parse(sbn.packageName, title, text) ?: return

        val decision = ruleEngine.evaluate(ride, rules)

        historyRepository.saveRide(ride, decision)

        if (decision is Decision.Accept) {
            acceptanceQueue.queueAccept(ride)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
