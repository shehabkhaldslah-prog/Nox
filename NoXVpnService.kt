package com.nox.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.nox.app.R
import com.nox.app.ui.MainActivity
import com.nox.app.utils.BlockedDomains
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class NoXVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val TAG = "NoXVpnService"

    companion object {
        const val CHANNEL_ID = "nox_vpn_channel"
        const val NOTIFICATION_ID = 1001
        var isActive = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        val builder = Builder()
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.3")   // Cloudflare for Families - blocks adult content
            .addDnsServer("1.0.0.3")   // Cloudflare for Families backup
            .setSession("NoX Protection")
            .setMtu(1500)

        vpnInterface = builder.establish()
        isActive = true
        isRunning = true

        // Start packet filtering in background thread
        Thread {
            runPacketFilter()
        }.start()

        Log.d(TAG, "NoX VPN Started - Protection Active")
    }

    private fun runPacketFilter() {
        val inputStream = FileInputStream(vpnInterface!!.fileDescriptor)
        val outputStream = FileOutputStream(vpnInterface!!.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)

        while (isRunning) {
            try {
                packet.clear()
                val length = inputStream.read(packet.array())
                if (length > 0) {
                    packet.limit(length)
                    // Forward packet (DNS is already filtered via Cloudflare Family DNS)
                    outputStream.write(packet.array(), 0, length)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Packet filter error: ${e.message}")
                break
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        isActive = false
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NoX حماية نشطة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "الحماية من المحتوى الضار نشطة"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("🛡️ NoX - الحماية نشطة")
                .setContentText("يتم حجب المحتوى الضار الآن")
                .setSmallIcon(R.drawable.ic_shield)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("🛡️ NoX - الحماية نشطة")
                .setContentText("يتم حجب المحتوى الضار الآن")
                .setSmallIcon(R.drawable.ic_shield)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Auto-restart if destroyed
        isRunning = false
        isActive = false
        val restartIntent = Intent(this, NoXVpnService::class.java)
        startService(restartIntent)
    }
}
