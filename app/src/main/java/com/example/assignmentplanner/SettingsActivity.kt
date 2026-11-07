package com.example.assignmentplanner

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.receiver.AlarmReceiver
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SettingsActivity : BaseActivity() {

    private var lastClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvSettingsTitle = findViewById<TextView>(R.id.tvSettingsTitle)
        tvSettingsTitle?.text = getString(R.string.settings_title)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: AppPreferences.getUserEmail(this) ?: "student@campus.ac.za"
        val userName = currentUser?.displayName ?: AppPreferences.getUserName(this) ?: "Student Account"
        
        findViewById<TextView>(R.id.tv_account_email)?.text = userEmail
        findViewById<TextView>(R.id.tv_account_name)?.text = userName

        // Theme Controls
        findViewById<MaterialButton>(R.id.btn_theme_atmospheric)?.setOnClickListener {
            onThemeSelected("ATMOSPHERIC")
        }
        findViewById<MaterialButton>(R.id.btn_theme_amoled)?.setOnClickListener {
            onThemeSelected("AMOLED")
        }
        findViewById<MaterialButton>(R.id.btn_theme_light)?.setOnClickListener {
            onThemeSelected("LIGHT")
        }

        // Language Controls
        findViewById<MaterialButton>(R.id.btn_lang_zu)?.setOnClickListener {
            onLanguageSelected("zu")
        }
        findViewById<MaterialButton>(R.id.btn_lang_en)?.setOnClickListener {
            onLanguageSelected("en")
        }

        updateHighlightState()

        // Push Notifications
        val notifSwitch = findViewById<SwitchCompat>(R.id.switch_notifications)
        notifSwitch?.isChecked = AppPreferences.isNotificationsEnabled(this)
        notifSwitch?.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setNotificationsEnabled(this, isChecked)
            if (!isChecked) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancelAll()
            }
        }

        // Haptics
        val hapticsSwitch = findViewById<SwitchCompat>(R.id.switch_haptics)
        hapticsSwitch?.isChecked = AppPreferences.isHapticsEnabled(this)
        hapticsSwitch?.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setHapticsEnabled(this, isChecked)
        }

        // Daily Chime
        val chimeSwitch = findViewById<SwitchCompat>(R.id.switch_daily_chime)
        chimeSwitch?.isChecked = AppPreferences.isDailyBriefingEnabled(this)
        chimeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setDailyBriefingEnabled(this, isChecked)
            setupDailyBriefingAlarm(isChecked)
        }

        // Gamification Toggle
        val gamificationSwitch = findViewById<SwitchCompat>(R.id.switchGamification)
        gamificationSwitch?.isChecked = AppPreferences.isGamificationEnabled(this)
        gamificationSwitch?.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setGamificationEnabled(this, isChecked)
        }

        // Reminder Selection
        val chipGroup = findViewById<ChipGroup>(R.id.cg_reminders)
        val defaultReminder = AppPreferences.getDefaultReminderOffset(this)
        when (defaultReminder) {
            24 -> chipGroup?.check(R.id.chip_24h)
            2 -> chipGroup?.check(R.id.chip_2h)
            1 -> chipGroup?.check(R.id.chip_1h)
            else -> chipGroup?.check(R.id.chip_due_time)
        }
        chipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val offset = when (checkedIds.first()) {
                    R.id.chip_24h -> 24
                    R.id.chip_2h -> 2
                    R.id.chip_1h -> 1
                    else -> 0
                }
                AppPreferences.setDefaultReminderOffset(this, offset)
            }
        }

        // Sync Logic
        val tvQueue = findViewById<TextView>(R.id.tv_sync_queue)
        
        lifecycleScope.launch(Dispatchers.IO) {
            val repo = PlannerRepository(AppDatabase.getDatabase(this@SettingsActivity).plannerDao())
            val count = repo.getPendingSyncRecordsCount()
            val lastSyncStr = AppPreferences.getLastSync(this@SettingsActivity)
            withContext(Dispatchers.Main) {
                if (count > 0) {
                    tvQueue?.text = "$count unsynced items"
                    tvQueue?.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                } else {
                    tvQueue?.text = "Last Synced: $lastSyncStr"
                    tvQueue?.setTextColor(android.graphics.Color.parseColor("#10B981"))
                }
            }
        }
        
        findViewById<Button>(R.id.btn_sync_now)?.setOnClickListener {
            tvQueue?.text = "Syncing..."
            Toast.makeText(this, "Syncing Room DB to Firestore...", Toast.LENGTH_SHORT).show()
            
            lifecycleScope.launch(Dispatchers.IO) {
                val repo = PlannerRepository(AppDatabase.getDatabase(this@SettingsActivity).plannerDao())
                repo.saveToFirestore()
                repo.syncFromFirestore()
                
                val sdf = SimpleDateFormat("HH:mm z", Locale.getDefault())
                val timeStr = sdf.format(Date())
                AppPreferences.setLastSync(this@SettingsActivity, timeStr)
                
                withContext(Dispatchers.Main) {
                    tvQueue?.text = "Last Synced: $timeStr"
                    tvQueue?.setTextColor(android.graphics.Color.parseColor("#10B981"))
                    Toast.makeText(this@SettingsActivity, "Sync Complete", Toast.LENGTH_SHORT).show()
                }
            }
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
        val tvSettingsTitle = findViewById<TextView>(R.id.tvSettingsTitle)
        tvSettingsTitle?.text = getString(R.string.settings_title)
        updateHighlightState()
    }

    private fun onLanguageSelected(newLangCode: String) {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime < 500) return
        lastClickTime = currentTime

        val currentLang = AppPreferences.getLanguage(this)
        if (currentLang == newLangCode) return

        AppPreferences.setLanguage(this, newLangCode)

        try {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(newLangCode)
            )
        } catch (e: Exception) { }

        finish()
        overridePendingTransition(0, 0)
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(0, 0)
    }

    private fun onThemeSelected(newTheme: String) {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime < 500) return
        lastClickTime = currentTime

        val currentTheme = AppPreferences.getThemeMode(this)
        if (currentTheme == newTheme) return

        AppPreferences.setThemeMode(this, newTheme)

        finish()
        overridePendingTransition(0, 0)
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(0, 0)
    }

    private fun setupDailyBriefingAlarm(enabled: Boolean) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = "ACTION_DAILY_BRIEFING"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (enabled) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun updateHighlightState() {
        val lang = AppPreferences.getLanguage(this)
        val theme = AppPreferences.getThemeMode(this)

        val btnZu = findViewById<MaterialButton>(R.id.btn_lang_zu)
        val btnEn = findViewById<MaterialButton>(R.id.btn_lang_en)

        if (lang == "zu") {
            btnZu?.strokeWidth = 3
            btnEn?.strokeWidth = 0
            btnZu?.setTextColor(getColor(R.color.text_primary))
            btnZu?.setStrokeColorResource(R.color.accent_cyan)
            btnEn?.setTextColor(getColor(R.color.text_on_surface_variant))
        } else {
            btnEn?.strokeWidth = 3
            btnZu?.strokeWidth = 0
            btnEn?.setTextColor(getColor(R.color.text_primary))
            btnEn?.setStrokeColorResource(R.color.accent_cyan)
            btnZu?.setTextColor(getColor(R.color.text_on_surface_variant))
        }

        val btnAtmo = findViewById<MaterialButton>(R.id.btn_theme_atmospheric)
        val btnAmoled = findViewById<MaterialButton>(R.id.btn_theme_amoled)
        val btnLight = findViewById<MaterialButton>(R.id.btn_theme_light)

        btnAtmo?.strokeWidth = 0
        btnAmoled?.strokeWidth = 0
        btnLight?.strokeWidth = 0
        btnAtmo?.setTextColor(getColor(R.color.text_on_surface_variant))
        btnAmoled?.setTextColor(getColor(R.color.text_on_surface_variant))
        btnLight?.setTextColor(getColor(R.color.text_on_surface_variant))

        when (theme) {
            "AMOLED" -> {
                btnAmoled?.strokeWidth = 3
                btnAmoled?.setStrokeColorResource(R.color.accent_cyan)
                btnAmoled?.setTextColor(getColor(R.color.text_primary))
            }
            "LIGHT" -> {
                btnLight?.strokeWidth = 3
                btnLight?.setStrokeColorResource(R.color.accent_cyan)
                btnLight?.setTextColor(getColor(R.color.text_primary))
            }
            else -> {
                btnAtmo?.strokeWidth = 3
                btnAtmo?.setStrokeColorResource(R.color.accent_cyan)
                btnAtmo?.setTextColor(getColor(R.color.text_primary))
            }
        }
    }
}
