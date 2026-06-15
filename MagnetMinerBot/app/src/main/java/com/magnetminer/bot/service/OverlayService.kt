package com.magnetminer.bot.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.magnetminer.bot.MainActivity
import com.magnetminer.bot.utils.BotState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayService : Service() {

    companion object {
        const val ACTION_STOP_OVERLAY = "com.magnetminer.bot.STOP_OVERLAY"
        const val CHANNEL_ID = "magnet_miner_bot_channel"
        const val NOTIF_ID = 1
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        addOverlayView()
        observeBotState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        AutoClickAccessibilityService.instance?.stopBot()
        BotState.setRunning(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun addOverlayView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 4, 4, 4)
        }

        val statusLabel = TextView(this).apply {
            text = "⏹ Durdu"
            setTextColor(Color.WHITE)
            textSize = 11f
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val toggleButton = TextView(this).apply {
            text = "BOT"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#43A047"))
            val size = (64 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            setPadding(8, 8, 8, 8)
        }

        container.addView(statusLabel)
        container.addView(toggleButton)

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var isDragging = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(container, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        val svc = AutoClickAccessibilityService.instance
                        if (svc == null) {
                            statusLabel.text = "⚠ A11y aç!"
                        } else if (BotState.isRunning.value) {
                            svc.stopBot()
                        } else {
                            svc.startBot()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        scope.launch {
            BotState.isRunning.collectLatest { running ->
                toggleButton.setBackgroundColor(
                    if (running) Color.parseColor("#E53935")
                    else Color.parseColor("#43A047")
                )
                statusLabel.text = if (running) "▶ Çalışıyor" else "⏹ Durdu"
            }
        }

        overlayView = container
        windowManager.addView(container, params)
    }

    private fun observeBotState() {
        scope.launch {
            BotState.tapCount.collectLatest { count ->
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotification(count))
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Magnet Miner Bot", NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(tapCount: Int = 0): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, OverlayService::class.java).apply { action = ACTION_STOP_OVERLAY },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Magnet Miner Bot")
            .setContentText("Tıklama: $tapCount")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_delete, "Durdur", stopIntent)
            .setOngoing(true)
            .build()
    }
}
