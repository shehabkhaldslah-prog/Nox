package com.nox.app.vpn

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.nox.app.R
import com.nox.app.utils.BlockedDomains

class AppMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // Check every second

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1002, buildNotification())
        handler.post(checkRunnable)
        return START_STICKY
    }

    private fun checkForegroundApp() {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 3000

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )

            if (stats != null && stats.isNotEmpty()) {
                val recentApp = stats.maxByOrNull { it.lastTimeUsed }
                val packageName = recentApp?.packageName ?: return

                if (BlockedDomains.isAppBlocked(packageName)) {
                    // Show block screen
                    val blockIntent = Intent(this, com.nox.app.ui.BlockedActivity::class.java)
                    blockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(blockIntent)
                }
            }
        } catch (e: Exception) {
            // Usage stats permission not granted
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "nox_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "NoX مراقبة التطبيقات",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("🛡️ NoX")
                .setContentText("مراقبة التطبيقات نشطة")
                .setSmallIcon(R.drawable.ic_shield)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("🛡️ NoX")
                .setContentText("مراقبة التطبيقات نشطة")
                .setSmallIcon(R.drawable.ic_shield)
                .setOngoing(true)
                .build()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        // Auto restart
        val restartIntent = Intent(this, AppMonitorService::class.java)
        startService(restartIntent)
    }
}
