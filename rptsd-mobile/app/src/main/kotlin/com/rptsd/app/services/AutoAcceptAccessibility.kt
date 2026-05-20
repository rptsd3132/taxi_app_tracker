package com.rptsd.app.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.rptsd.app.data.repository.RulesRepository
import com.rptsd.app.engine.HumanBehavior
import com.rptsd.app.domain.model.RideData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AutoAcceptAccessibility : AccessibilityService() {

    @Inject lateinit var acceptanceQueue: AcceptanceQueue
    @Inject lateinit var rulesRepository: RulesRepository

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastUserTouchTime = 0L
    private val recentAcceptTimes = mutableListOf<Long>()

    private val acceptButtonTexts = listOf(
        "Accept", "ACCEPT", "Accept Trip", "Confirm", "CONFIRM",
        "Take Trip", "Take Ride", "GO", "Start",
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope.launch {
            acceptanceQueue.queue.collect { ride ->
                handleAcceptance(ride)
            }
        }
    }

    private suspend fun handleAcceptance(ride: RideData) {
        val rules = rulesRepository.observeRules().first()
        if (!rules.isAutoAcceptEnabled) return

        val now = System.currentTimeMillis()

        // Rate limit: max 5 accepts per minute
        recentAcceptTimes.removeAll { now - it > 60_000 }
        if (recentAcceptTimes.size >= 5) return

        // Back off if user is actively touching the screen
        if (now - lastUserTouchTime < 30_000) return

        // Human-like random delay before acting
        delay(HumanBehavior.getRandomDelay())

        val rootNode = rootInActiveWindow ?: return
        val acceptNode = findAcceptButton(rootNode)
        rootNode.recycle()

        if (acceptNode == null) return

        performTap(acceptNode)
        acceptNode.recycle()
        recentAcceptTimes.add(System.currentTimeMillis())
    }

    private fun findAcceptButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (text in acceptButtonTexts) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            val clickable = nodes.firstOrNull { it.isClickable || it.isEnabled }
            if (clickable != null) {
                nodes.filter { it !== clickable }.forEach { it.recycle() }
                return clickable
            }
            nodes.forEach { it.recycle() }
        }
        return null
    }

    private fun performTap(node: AccessibilityNodeInfo) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val (offsetX, offsetY) = HumanBehavior.getRandomOffset()
        val x = bounds.exactCenterX() + offsetX
        val y = bounds.exactCenterY() + offsetY

        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 100L))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START) {
            lastUserTouchTime = System.currentTimeMillis()
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
