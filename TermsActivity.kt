package com.nox.app.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nox.app.R
import com.nox.app.admin.NoXDeviceAdminReceiver
import com.nox.app.vpn.AppMonitorService
import com.nox.app.vpn.NoXVpnService

class TermsActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private val REQUEST_ADMIN = 100
    private val REQUEST_VPN = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = NoXDeviceAdminReceiver.getComponentName(this)

        val agreeCheckbox = findViewById<CheckBox>(R.id.checkbox_agree)
        val activateButton = findViewById<Button>(R.id.btn_activate)

        activateButton.setOnClickListener {
            if (!agreeCheckbox.isChecked) {
                Toast.makeText(this, "يجب الموافقة على الشروط أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            activateProtection()
        }
    }

    private fun activateProtection() {
        // Step 1: Request Device Admin
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "يحتاج NoX لصلاحية مسؤول الجهاز لمنع إلغاء تثبيته وحماية إعداداته."
                )
            }
            startActivityForResult(intent, REQUEST_ADMIN)
        } else {
            requestVpnPermission()
        }
    }

    private fun requestVpnPermission() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, REQUEST_VPN)
        } else {
            onVpnPermissionGranted()
        }
    }

    private fun onVpnPermissionGranted() {
        // Save activation state
        val prefs = getSharedPreferences("nox_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("protection_activated", true).apply()

        // Start VPN Service
        val vpnIntent = Intent(this, NoXVpnService::class.java)
        startForegroundService(vpnIntent)

        // Start App Monitor
        val monitorIntent = Intent(this, AppMonitorService::class.java)
        startForegroundService(monitorIntent)

        // Go to main screen
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ADMIN -> {
                if (devicePolicyManager.isAdminActive(adminComponent)) {
                    requestVpnPermission()
                } else {
                    // Force retry - admin is required
                    Toast.makeText(this, "صلاحية مسؤول الجهاز مطلوبة للحماية", Toast.LENGTH_LONG).show()
                    activateProtection()
                }
            }
            REQUEST_VPN -> {
                if (resultCode == RESULT_OK) {
                    onVpnPermissionGranted()
                } else {
                    Toast.makeText(this, "إذن VPN مطلوب للحماية", Toast.LENGTH_LONG).show()
                    requestVpnPermission()
                }
            }
        }
    }

    // Prevent going back before activation
    override fun onBackPressed() {
        Toast.makeText(this, "يجب تفعيل الحماية أولاً", Toast.LENGTH_SHORT).show()
    }
}
