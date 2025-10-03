package com.example.miscitasmedicas

import android.content.Context
import org.json.JSONArray

private const val PREFS_APPOINTMENTS = "appointments_storage"
private const val KEY_APPOINTMENTS = "appointments"

/**
 * Maneja el almacenamiento simple de citas usando SharedPreferences.
 */
class AppointmentStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_APPOINTMENTS, Context.MODE_PRIVATE)

    fun saveAppointment(appointment: Appointment) {
        val current = getAppointments().toMutableList()
        current.add(0, appointment)

        val array = JSONArray()
        current.forEach { array.put(it.toJson()) }

        prefs.edit().putString(KEY_APPOINTMENTS, array.toString()).apply()
    }

    fun getAppointments(): List<Appointment> {
        val stored = prefs.getString(KEY_APPOINTMENTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(stored)
            buildList {
                for (i in 0 until array.length()) {
                    val appointment = Appointment.fromJson(array.optJSONObject(i))
                    if (appointment != null) {
                        add(appointment)
                    }
                }
            }
        } catch (exception: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_APPOINTMENTS).apply()
    }
}
