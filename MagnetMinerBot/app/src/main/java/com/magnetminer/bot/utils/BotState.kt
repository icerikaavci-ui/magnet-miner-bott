package com.magnetminer.bot.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BotState {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _tapCount = MutableStateFlow(0)
    val tapCount: StateFlow<Int> = _tapCount.asStateFlow()

    private val _lastTappedLabel = MutableStateFlow("")
    val lastTappedLabel: StateFlow<String> = _lastTappedLabel.asStateFlow()

    private val _logLines = MutableStateFlow<List<String>>(emptyList())
    val logLines: StateFlow<List<String>> = _logLines.asStateFlow()

    fun setRunning(value: Boolean) {
        _isRunning.value = value
    }

    fun recordTap(label: String) {
        _tapCount.value += 1
        _lastTappedLabel.value = label
        val line = "[${_tapCount.value}] Tapped: $label"
        _logLines.value = (_logLines.value + line).takeLast(50)
    }

    fun reset() {
        _isRunning.value = false
        _tapCount.value = 0
        _lastTappedLabel.value = ""
        _logLines.value = emptyList()
    }

    fun appendLog(msg: String) {
        _logLines.value = (_logLines.value + msg).takeLast(50)
    }
}
