package com.example.miscitasmedicas

import android.content.Context

private const val PREFS_NAME = "reminder_preferences"
private const val KEY_ENABLED = "enabled"

/**
 * Maneja la preferencia del usuario para activar o desactivar los recordatorios
 * en segundo plano.
 */
class ReminderPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, true)
}
