package com.nox.app.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nox.app.R
import com.nox.app.admin.NoXDeviceAdminReceiver
import com.nox.app.vpn.NoXVpnService
import com.nox.app.vpn.AppMonitorService

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = NoXDeviceAdminReceiver.getComponentName(this)

        // Check if re-enable needed
        if (intent.getBooleanExtra("re_enable_admin", false)) {
            reEnableAdmin()
        }

        updateStatus()
        ensureServicesRunning()
    }

    private fun updateStatus() {
        val statusText = findViewById<TextView>(R.id.tv_status)
        val adminText = findViewById<TextView>(R.id.tv_admin_status)
        val vpnText = findViewById<TextView>(R.id.tv_vpn_status)

        val isAdminActive = devicePolicyManager.isAdminActive(adminComponent)
        val isVpnActive = NoXVpnService.isActive

        statusText.text = if (isAdminActive && isVpnActive) "✅ الحماية نشطة" else "⚠️ الحماية غير مكتملة"
        adminText.text = if (isAdminActive) "✅ منع الحذف: مفعّل" else "❌ منع الحذف: معطّل"
        vpnText.text = if (isVpnActive) "✅ حجب المواقع: مفعّل" else "❌ حجب المواقع: معطّل"
    }

    private fun ensureServicesRunning() {
        val vpnIntent = Intent(this, NoXVpnService::class.java)
        startForegroundService(vpnIntent)

        val monitorIntent = Intent(this, AppMonitorService::class.java)
        startForegroundService(monitorIntent)
    }

    private fun reEnableAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "يجب تفعيل صلاحية مسؤول الجهاز للحفاظ على الحماية."
            )
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    // Prevent going back
    override fun onBackPressed() {
        // Do nothing - can't exit
    }
}
