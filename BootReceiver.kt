package com.nox.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nox.app.vpn.AppMonitorService
import com.nox.app.vpn.NoXVpnService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_REBOOT) {

            val prefs = context.getSharedPreferences("nox_prefs", Context.MODE_PRIVATE)
            val isActivated = prefs.getBoolean("protection_activated", false)

            if (isActivated) {
                // Restart VPN service
                val vpnIntent = Intent(context, NoXVpnService::class.java)
                context.startForegroundService(vpnIntent)

                // Restart App Monitor
                val monitorIntent = Intent(context, AppMonitorService::class.java)
                context.startForegroundService(monitorIntent)
            }
        }
    }
}
