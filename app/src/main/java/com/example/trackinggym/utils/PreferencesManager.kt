package com.example.trackinggym.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastExportTimeMs: Long
        get() = prefs.getLong(KEY_LAST_EXPORT_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_EXPORT_TIME, value).apply()

    var lastReminderTimeMs: Long
        get() = prefs.getLong(KEY_LAST_REMINDER_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_REMINDER_TIME, value).apply()

    companion object {
        private const val PREFS_NAME = "trackinggym_prefs"
        private const val KEY_LAST_EXPORT_TIME = "last_export_time"
        private const val KEY_LAST_REMINDER_TIME = "last_reminder_time"
    }
}
