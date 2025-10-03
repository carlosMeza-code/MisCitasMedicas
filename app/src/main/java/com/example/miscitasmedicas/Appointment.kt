package com.example.miscitasmedicas

import org.json.JSONObject

private const val KEY_PATIENT_NAME = "patient_name"
private const val KEY_SPECIALTY = "specialty"
private const val KEY_DATE = "date"
private const val KEY_TIME = "time"
private const val KEY_NOTES = "notes"

/**
 * Representa una cita médica agendada por el usuario.
 */
data class Appointment(
    val patientName: String,
    val specialty: String,
    val date: String,
    val time: String,
    val notes: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put(KEY_PATIENT_NAME, patientName)
        put(KEY_SPECIALTY, specialty)
        put(KEY_DATE, date)
        put(KEY_TIME, time)
        put(KEY_NOTES, notes)
    }

    companion object {
        fun fromJson(json: JSONObject?): Appointment? {
            if (json == null) return null
            val patientName = json.optString(KEY_PATIENT_NAME)
            val specialty = json.optString(KEY_SPECIALTY)
            val date = json.optString(KEY_DATE)
            val time = json.optString(KEY_TIME)
            if (patientName.isNullOrBlank() || specialty.isNullOrBlank() || date.isNullOrBlank() || time.isNullOrBlank()) {
                return null
            }
            val notes = json.optString(KEY_NOTES)
            return Appointment(
                patientName = patientName,
                specialty = specialty,
                date = date,
                time = time,
                notes = notes
            )
        }
    }
}
