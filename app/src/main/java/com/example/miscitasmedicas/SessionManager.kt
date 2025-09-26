package com.example.miscitasmedicas

import android.content.Context

private const val PREFS_NAME = "user_session"
private const val KEY_NAME = "name"
private const val KEY_EMAIL = "email"
private const val KEY_PASSWORD = "password"

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PASSWORD, user.password)
            .apply()
    }

    fun getUser(): User? {
        val name = prefs.getString(KEY_NAME, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        return if (!name.isNullOrBlank() && !email.isNullOrBlank() && !password.isNullOrBlank()) {
            User(name = name, email = email, password = password)
        } else {
            null
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}


data class User(
    val name: String,
    val email: String,
    val password: String
)
