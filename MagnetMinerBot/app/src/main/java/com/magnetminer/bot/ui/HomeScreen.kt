package com.magnetminer.bot.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magnetminer.bot.service.AutoClickAccessibilityService
import com.magnetminer.bot.service.OverlayService
import com.magnetminer.bot.utils.BotState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val isRunning by BotState.isRunning.collectAsState()
    val tapCount by BotState.tapCount.collectAsState()
    val lastLabel by BotState.lastTappedLabel.collectAsState()
    val logLines by BotState.logLines.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) {
            coroutineScope.launch { listState.animateScrollToItem(logLines.size - 1) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Magnet Miner Bot", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        StatusCard(isRunning = isRunning, tapCount = tapCount, lastLabel = lastLabel)

        val a11yEnabled = AutoClickAccessibilityService.instance != null
        if (!a11yEnabled) {
            AccessibilityWarning(onOpenSettings = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            })
        }

        Button(
            onClick = {
                if (!Settings.canDrawOverlays(context)) {
