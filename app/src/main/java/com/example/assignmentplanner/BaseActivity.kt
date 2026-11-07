package com.example.assignmentplanner

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

open class BaseActivity : AppCompatActivity() {

    private var appliedThemeMode: String = ""
    private var appliedLanguageCode: String = ""

    override fun attachBaseContext(newBase: Context) {
        val langCode = AppPreferences.getLanguage(newBase)
        val locale = java.util.Locale(langCode)
        java.util.Locale.setDefault(locale)

        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeMode = AppPreferences.getThemeMode(this)
        when (themeMode) {
            "LIGHT" -> {
                setTheme(R.style.Theme_AssignmentPlanner_Light)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
            "AMOLED" -> {
                setTheme(R.style.Theme_AssignmentPlanner_Amoled)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
            else -> {
                setTheme(R.style.Theme_AssignmentPlanner_Atmospheric)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
        appliedThemeMode = themeMode
        appliedLanguageCode = AppPreferences.getLanguage(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val latestTheme = AppPreferences.getThemeMode(this)
        val latestLang = AppPreferences.getLanguage(this)

        if (appliedThemeMode.isNotEmpty() && (latestTheme != appliedThemeMode || latestLang != appliedLanguageCode)) {
            appliedThemeMode = latestTheme
            appliedLanguageCode = latestLang
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            return
        }

        setupRadialMenu()
        setupTopBar()
    }

    fun applyThemeSurface(view: View) {
        val isLight = AppPreferences.getThemeMode(this) == "LIGHT"
        if (isLight) {
            if (view is com.google.android.material.card.MaterialCardView) {
                view.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                view.strokeColor = Color.parseColor("#E2E8F0")
                view.strokeWidth = 2
            } else {
                view.setBackgroundColor(Color.parseColor("#F8FAFC"))
            }
        }
    }

    fun updateTopBarInitials(avatarTextView: TextView?) {
        if (avatarTextView != null) {
            avatarTextView.text = AppPreferences.getUserInitials(this)
        }
    }

    private fun setupTopBar() {
        val tvInitials = findViewById<TextView>(R.id.tv_profile_initials)
            ?: findViewById<TextView>(R.id.tv_header_title)

        updateTopBarInitials(tvInitials)

        val badge = findViewById<View>(R.id.badge_unread)
        if (badge != null) {
            val hasUnread = AppPreferences.hasUnreadNotifications(this)
            badge.visibility = if (hasUnread) View.VISIBLE else View.GONE
        }

        val openProfile = View.OnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<View>(R.id.fl_profile_avatar)?.setOnClickListener(openProfile)
        findViewById<View>(R.id.tv_profile_initials)?.setOnClickListener(openProfile)
        findViewById<View>(R.id.fl_notifications)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        findViewById<View>(R.id.iv_notifications)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }

    private fun setupRadialMenu() {
        val fabAdd = findViewById<View>(R.id.fab_add)
        fabAdd?.setOnClickListener {
            val dialog = RadialMenuDialog(this)
            dialog.show()
        }
    }

    fun setupTopBarProfile(avatarView: TextView) {
        avatarView.text = AppPreferences.getUserInitials(this)
    }

    fun setupCustomBottomNavigation(activeNavId: Int = -1) {
        val navDashboard = findViewById<View>(R.id.nav_dashboard)
        val navCalendar = findViewById<View>(R.id.nav_calendar)
        val navAssignments = findViewById<View>(R.id.nav_assignments)
        val navSettings = findViewById<View>(R.id.nav_settings)
        val fabAdd = findViewById<View>(R.id.fab_add)

        val isLight = AppPreferences.getThemeMode(this) == "LIGHT"
        val accentColor = if (isLight) Color.parseColor("#2563EB") else Color.parseColor("#00E5FF")
        val inactiveColor = if (isLight) Color.parseColor("#64748B") else Color.parseColor("#94A3B8")

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
