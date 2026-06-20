package com.nox.app.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NoXDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, NoXDeviceAdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device Admin activated - save status
        val prefs = context.getSharedPreferences("nox_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("admin_active", true).apply()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Block any disable attempt
        return "لا يمكن إلغاء تفعيل هذا التطبيق. تم تفعيل الحماية."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // If somehow disabled, immediately re-enable
        val prefs = context.getSharedPreferences("nox_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("admin_active", false).apply()

        // Try to re-enable immediately
        val reEnableIntent = Intent(context, com.nox.app.ui.MainActivity::class.java)
        reEnableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        reEnableIntent.putExtra("re_enable_admin", true)
        context.startActivity(reEnableIntent)
    }
}
