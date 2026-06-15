package com.magnetminer.bot.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object BotPreferences {

    private const val PREFS_NAME = "magnet_miner_bot_prefs"
    private const val KEY_TAP_INTERVAL_MS = "tap_interval_ms"
    private const val KEY_ENABLED_LABELS = "enabled_labels"
    private const val KEY_SCAN_INTERVAL_MS = "scan_interval_ms"

    val DEFAULT_LABELS = listOf("Play", "Claim", "Collect", "Continue", "Reward", "X3")
    const val DEFAULT_TAP_INTERVAL_MS = 500L
    const val DEFAULT_SCAN_INTERVAL_MS = 300L

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTapIntervalMs(context: Context): Long =
        prefs(context).getLong(KEY_TAP_INTERVAL_MS, DEFAULT_TAP_INTERVAL_MS)

    fun setTapIntervalMs(context: Context, value: Long) {
        prefs(context).edit { putLong(KEY_TAP_INTERVAL_MS, value) }
    }

    fun getScanIntervalMs(context: Context): Long =
        prefs(context).getLong(KEY_SCAN_INTERVAL_MS, DEFAULT_SCAN_INTERVAL_MS)

    fun setScanIntervalMs(context: Context, value: Long) {
        prefs(context).edit { putLong(KEY_SCAN_INTERVAL_MS, value) }
    }

    fun getEnabledLabels(context: Context): Set<String> {
        val stored = prefs(context).getStringSet(KEY_ENABLED_LABELS, null)
        return stored ?: DEFAULT_LABELS.toSet()
    }

    fun setEnabledLabels(context: Context, labels: Set<String>) {
        prefs(context).edit { putStringSet(KEY_ENABLED_LABELS, labels) }
    }
}
