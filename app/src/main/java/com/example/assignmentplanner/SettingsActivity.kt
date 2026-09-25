package com.example.assignmentplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Theme Controls
        findViewById<MaterialButton>(R.id.btn_theme_atmospheric)?.setOnClickListener {
            AppPreferences.setTheme(this, "ATMOSPHERIC")
            recreate()
        }
        findViewById<MaterialButton>(R.id.btn_theme_amoled)?.setOnClickListener {
            AppPreferences.setTheme(this, "AMOLED")
            recreate()
        }
        findViewById<MaterialButton>(R.id.btn_theme_light)?.setOnClickListener {
            AppPreferences.setTheme(this, "LIGHT")
            recreate()
        }

        // Initialize Language Controls
        findViewById<MaterialButton>(R.id.btn_lang_zu)?.setOnClickListener {
            AppPreferences.setEnglish(this, false)
            recreate()
        }
        findViewById<MaterialButton>(R.id.btn_lang_en)?.setOnClickListener {
            AppPreferences.setEnglish(this, true)
            recreate()
        }

        // Load & Save Push Notif
        val prefs = AppPreferences.getPrefs(this)
        val notifSwitch = findViewById<SwitchCompat>(R.id.switch_notifications)
        notifSwitch?.isChecked = prefs.getBoolean("push_enabled", true)
        notifSwitch?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("push_enabled", isChecked).apply()
        }

        // Load & Save Haptics
        val hapticsSwitch = findViewById<SwitchCompat>(R.id.switch_haptics)
        hapticsSwitch?.isChecked = prefs.getBoolean("haptics_enabled", true)
        hapticsSwitch?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("haptics_enabled", isChecked).apply()
        }

        // Load & Save Daily Chime
        val chimeSwitch = findViewById<SwitchCompat>(R.id.switch_daily_chime)
        chimeSwitch?.isChecked = prefs.getBoolean("daily_chime_enabled", true)
        chimeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("daily_chime_enabled", isChecked).apply()
        }

        // Reminder Selection
        val chipGroup = findViewById<ChipGroup>(R.id.cg_reminders)
        val defaultReminder = prefs.getInt("reminder_timing", R.id.chip_24h)
        chipGroup?.check(defaultReminder)
        chipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                prefs.edit().putInt("reminder_timing", checkedIds.first()).apply()
            }
        }

        // Sync Logic
        val tvQueue = findViewById<TextView>(R.id.tv_sync_queue)
        tvQueue?.text = AppPreferences.getLastSync(this)
        
        findViewById<Button>(R.id.btn_sync_now)?.setOnClickListener {
            tvQueue?.text = "Syncing..."
            Toast.makeText(this, "Syncing Room DB to REST API...", Toast.LENGTH_SHORT).show()
            
            // Mock network delay & completion
            it.postDelayed({
                val timeStr = "10:42 SAST" // Or format current time
                AppPreferences.setLastSync(this, timeStr)
                tvQueue?.text = timeStr
                Toast.makeText(this, "Sync Complete", Toast.LENGTH_SHORT).show()
            }, 1500)
        }

        findViewById<Button>(R.id.btn_logout)?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Bottom Navigation Logic
        setupCustomBottomNavigation(R.id.nav_settings)
    }

    override fun onResume() {
        super.onResume()
        setupCustomBottomNavigation(R.id.nav_settings)
    }
}
