package com.nox.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.nox.app.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("nox_prefs", MODE_PRIVATE)
        val isActivated = prefs.getBoolean("protection_activated", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isActivated) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, TermsActivity::class.java))
            }
            finish()
        }, 2500)
    }
}
