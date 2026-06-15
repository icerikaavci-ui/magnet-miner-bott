package com.magnetminer.bot.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
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
        const val ACTION_START = "com.magnetminer.bot.START"
        const val ACTION_STOP  = "com.magnetminer.bot.STOP"

        @Volatile
        var instance: AutoClickAccessibilityService? = null
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var scanJob: Job? = null
    private var lastTapTimeMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        BotState.appendLog("Erişilebilirlik servisi bağlandı.")
    }

    override fun onInterrupt() {
        BotState.appendLog("Servis kesintiye uğradı.")
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
