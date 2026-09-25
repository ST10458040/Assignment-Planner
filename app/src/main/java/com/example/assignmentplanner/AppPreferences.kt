package com.example.assignmentplanner

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "AppPreferences"
    
    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isEnglish(context: Context): Boolean {
        return getPrefs(context).getBoolean("english_only", false)
    }

    fun setEnglish(context: Context, isEnglish: Boolean) {
        getPrefs(context).edit().putBoolean("english_only", isEnglish).apply()
    }

    fun getTheme(context: Context): String {
        return getPrefs(context).getString("app_theme", "ATMOSPHERIC") ?: "ATMOSPHERIC"
    }

    fun setTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString("app_theme", theme).apply()
    }
    
    fun getLastSync(context: Context): String {
        return getPrefs(context).getString("last_sync", "10:42 SAST") ?: "10:42 SAST"
    }
    
    fun setLastSync(context: Context, time: String) {
        getPrefs(context).edit().putString("last_sync", time).apply()
    }
}
