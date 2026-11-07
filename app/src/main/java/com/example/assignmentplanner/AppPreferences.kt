package com.example.assignmentplanner

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "AppPreferences"
    
    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLanguage(context: Context): String {
        return getPrefs(context).getString("app_language", "en") ?: "en"
    }

    fun setLanguage(context: Context, langCode: String) {
        getPrefs(context).edit().putString("app_language", langCode).apply()
    }

    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString("app_theme", "ATMOSPHERIC") ?: "ATMOSPHERIC"
    }

    fun setThemeMode(context: Context, themeKey: String) {
        getPrefs(context).edit().putString("app_theme", themeKey).apply()
    }
    
    fun getLastSync(context: Context): String {
        return getPrefs(context).getString("last_sync", "10:42 SAST") ?: "10:42 SAST"
    }
    
    fun setLastSync(context: Context, time: String) {
        getPrefs(context).edit().putString("last_sync", time).apply()
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("push_enabled", true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("push_enabled", enabled).apply()
    }

    fun isHapticsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("haptics_enabled", true)
    }

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("haptics_enabled", enabled).apply()
    }

    fun getDefaultReminderOffset(context: Context): Int {
        return getPrefs(context).getInt("reminder_offset_hours", 24)
    }

    fun setDefaultReminderOffset(context: Context, offsetHours: Int) {
        getPrefs(context).edit().putInt("reminder_offset_hours", offsetHours).apply()
    }

    fun isDailyBriefingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("daily_chime_enabled", true)
    }

    fun setDailyBriefingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("daily_chime_enabled", enabled).apply()
    }

    fun isGamificationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("gamification_enabled", true)
    }

    fun setGamificationEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("gamification_enabled", enabled).apply()
    }

    fun hasUnreadNotifications(context: Context): Boolean {
        return getPrefs(context).getBoolean("has_unread_notifications", false)
    }

    fun setHasUnreadNotifications(context: Context, hasUnread: Boolean) {
        getPrefs(context).edit().putBoolean("has_unread_notifications", hasUnread).apply()
    }

    fun triggerHapticFeedback(context: Context) {
        if (isHapticsEnabled(context)) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString("user_email", null)
    }

    fun getUserName(context: Context): String? {
        val firstName = getPrefs(context).getString("user_first_name", "Student")
        val lastName = getPrefs(context).getString("user_last_name", "")
        return "$firstName $lastName".trim()
    }

    // Academic Record getters & setters
    fun getUserFullName(context: Context): String {
        return getPrefs(context).getString("user_full_name", "Mlondi Shezi") ?: "Mlondi Shezi"
    }

    fun setUserFullName(context: Context, name: String) {
        val parts = name.trim().split("\\s+".toRegex(), limit = 2)
        val firstName = parts.getOrNull(0) ?: "Mlondi"
        val lastName = parts.getOrNull(1) ?: ""
        getPrefs(context).edit()
            .putString("user_full_name", name)
            .putString("user_first_name", firstName)
            .putString("user_last_name", lastName)
            .apply()
    }

    fun getUserInitials(context: Context): String {
        val fullName = getUserFullName(context).trim()
        if (fullName.isEmpty()) return "MS"
        val parts = fullName.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "MS"
        }
    }

    fun getStudentNumber(context: Context): String {
        return getPrefs(context).getString("student_number", "ST10458040") ?: "ST10458040"
    }

    fun setStudentNumber(context: Context, number: String) {
        getPrefs(context).edit().putString("student_number", number).apply()
    }

    fun getFaculty(context: Context): String {
        return getPrefs(context).getString("user_faculty", "Faculty of Information Technology") ?: "Faculty of Information Technology"
    }

    fun setFaculty(context: Context, faculty: String) {
        getPrefs(context).edit().putString("user_faculty", faculty).apply()
    }

    fun getDegreeYear(context: Context): String {
        return getPrefs(context).getString("degree_year", "Bachelor of IT • Year 3 (NQF 7)") ?: "Bachelor of IT • Year 3 (NQF 7)"
    }

    fun setDegreeYear(context: Context, degree: String) {
        getPrefs(context).edit().putString("degree_year", degree).apply()
    }
}
