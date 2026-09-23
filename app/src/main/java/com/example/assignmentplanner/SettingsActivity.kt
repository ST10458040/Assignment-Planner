package com.example.assignmentplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val notifSwitch = findViewById<SwitchCompat>(R.id.switch_notifications)
        
        // Load preferences
        notifSwitch?.isChecked = sharedPreferences.getBoolean("notifications_enabled", true)
        
        notifSwitch?.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_sync_now)?.setOnClickListener {
            val tvQueue = findViewById<TextView>(R.id.tv_sync_queue)
            tvQueue?.text = "SYNCING..."
            Toast.makeText(this, "Syncing Room DB to REST API...", Toast.LENGTH_SHORT).show()
            
            // Mock network delay
            it.postDelayed({
                tvQueue?.text = "ALL SYNCED"
                Toast.makeText(this, "Sync Complete", Toast.LENGTH_SHORT).show()
            }, 1500)
        }

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_settings
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    false
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    false
                }
                R.id.nav_assignments -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    false
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = R.id.nav_settings
    }
}
