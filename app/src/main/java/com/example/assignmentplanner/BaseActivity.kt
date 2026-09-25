package com.example.assignmentplanner

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        setupRadialMenu()
        applyLanguage()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val themeMode = prefs.getString("theme_mode", "atmospheric")
        val mainView = findViewById<View>(R.id.main)
        
        when (themeMode) {
            "amoled" -> mainView?.setBackgroundColor(Color.BLACK)
            "light" -> mainView?.setBackgroundColor(Color.WHITE)
            else -> mainView?.setBackgroundResource(R.color.bg_gradient_start)
        }
    }

    private fun setupRadialMenu() {
        val fabAdd = findViewById<View>(R.id.fab_add)
        fabAdd?.setOnClickListener {
            val dialog = RadialMenuDialog(this)
            dialog.show()
        }
    }

    fun applyLanguage() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isEnglish = prefs.getBoolean("is_english", false)

        val headerTitle = findViewById<TextView>(R.id.tv_header_title)
        val syncStatus = findViewById<TextView>(R.id.tv_sync_status)
        val logoutBtn = findViewById<Button>(R.id.btn_logout)

        if (isEnglish) {
            headerTitle?.text = headerTitle?.text?.toString()?.substringAfter(" / ")?.substringBefore(" ") ?: headerTitle?.text
            syncStatus?.text = "Synced"
            logoutBtn?.text = "Log Out"
        } else {
            // Very simplified dual language restore for mock purposes
            if (headerTitle?.text?.contains(" / ") == false) {
                // Keep default if missing
            }
        }
    }

    fun setupCustomBottomNavigation(activeNavId: Int = -1) {
        val navDashboard = findViewById<View>(R.id.nav_dashboard)
        val navCalendar = findViewById<View>(R.id.nav_calendar)
        val navAssignments = findViewById<View>(R.id.nav_assignments)
        val navSettings = findViewById<View>(R.id.nav_settings)
        val fabAdd = findViewById<View>(R.id.fab_add)

        val accentColor = Color.parseColor("#00E5FF") // Active Cyan
        val inactiveColor = Color.parseColor("#94A3B8")

        // Set colors based on active item
        val icons = mapOf(
            R.id.nav_dashboard to Pair(findViewById<ImageView>(R.id.icon_dashboard), findViewById<TextView>(R.id.text_dashboard)),
            R.id.nav_calendar to Pair(findViewById<ImageView>(R.id.icon_calendar), findViewById<TextView>(R.id.text_calendar)),
            R.id.nav_assignments to Pair(findViewById<ImageView>(R.id.icon_assignments), findViewById<TextView>(R.id.text_assignments)),
            R.id.nav_settings to Pair(findViewById<ImageView>(R.id.icon_settings), findViewById<TextView>(R.id.text_settings))
        )

        icons.forEach { (id, views) ->
            val color = if (id == activeNavId) accentColor else inactiveColor
            views.first?.setColorFilter(color)
            views.second?.setTextColor(color)
            if (id == activeNavId) views.second?.setTypeface(null, Typeface.BOLD)
        }

        navDashboard?.setOnClickListener {
            if (activeNavId != R.id.nav_dashboard) {
                startActivity(Intent(this, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                overridePendingTransition(0, 0)
            }
        }
        navCalendar?.setOnClickListener {
            if (activeNavId != R.id.nav_calendar) {
                startActivity(Intent(this, CalendarActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                overridePendingTransition(0, 0)
            }
        }
        navAssignments?.setOnClickListener {
            if (activeNavId != R.id.nav_assignments) {
                startActivity(Intent(this, AssignmentsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                overridePendingTransition(0, 0)
            }
        }
        navSettings?.setOnClickListener {
            if (activeNavId != R.id.nav_settings) {
                startActivity(Intent(this, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                overridePendingTransition(0, 0)
            }
        }
        
        fabAdd?.setOnClickListener {
            val dialog = RadialMenuDialog(this)
            dialog.show()
        }
    }
}