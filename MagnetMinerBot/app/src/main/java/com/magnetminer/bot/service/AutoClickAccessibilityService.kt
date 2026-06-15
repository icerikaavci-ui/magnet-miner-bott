package com.magnetminer.bot.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.magnetminer.bot.utils.BotPreferences
import com.magnetminer.bot.utils.BotState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: AutoClickAccessibilityService? = null
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var scanJob: Job? = null
    private var lastTapTimeMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        BotState.appendLog("Servis bağlandı.")
    }

    override fun onInterrupt() {
        BotState.appendLog("Servis kesildi.")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!BotState.isRunning.value) return
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> scanOnce()
            else -> Unit
        }
    }

    fun startBot() {
        BotState.setRunning(true)
        BotState.appendLog("Bot başlatıldı.")
        startScanning()
    }

    fun stopBot() {
        BotState.setRunning(false)
        stopScanning()
        BotState.appendLog("Bot durduruldu.")
    }

    private fun startScanning() {
        scanJob?.cancel()
        scanJob = serviceScope.launch {
            while (BotState.isRunning.value) {
                scanOnce()
                val interval = BotPreferences.getScanIntervalMs(this@AutoClickAccessibilityService)
                delay(interval)
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    private fun scanOnce() {
        val root = rootInActiveWindow ?: return
        val enabledLabels = BotPreferences.getEnabledLabels(this)
        val tapInterval = BotPreferences.getTapIntervalMs(this)
        val now = System.currentTimeMillis()
        if (now - lastTapTimeMs < tapInterval) return
        findAndClick(root, enabledLabels, now)
        root.recycle()
    }

    private fun findAndClick(node: AccessibilityNodeInfo, labels: Set<String>, now: Long): Boolean {
        val nodeText = node.text?.toString()?.trim() ?: ""
        val nodeDesc = node.contentDescription?.toString()?.trim() ?: ""
        val matched = labels.firstOrNull { label ->
            nodeText.equals(label, ignoreCase = true) ||
            nodeDesc.equals(label, ignoreCase = true) ||
            nodeText.contains(label, ignoreCase = true)
        }
        if (matched != null && (node.isClickable || hasClickableParent(node))) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (!bounds.isEmpty) {
                performTapGesture(bounds.centerX().toFloat(), bounds.centerY().toFloat())
                lastTapTimeMs = now
                BotState.recordTap(matched)
                return true
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findAndClick(child, labels, now)
            child.recycle()
            if (found) return true
        }
        return false
    }

    private fun hasClickableParent(node: AccessibilityNodeInfo): Boolean {
        val parent = node.parent ?: return false
        return try { parent.isClickable } finally { parent.recycle() }
    }

    private fun performTapGesture(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 50L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
