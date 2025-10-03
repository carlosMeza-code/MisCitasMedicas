package com.example.miscitasmedicas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.MaterialToolbar

class AppointmentsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var emptyState: View
    private lateinit var scrollContent: NestedScrollView
    private lateinit var listAppointments: LinearLayout
    private lateinit var storage: AppointmentStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        toolbar = findViewById(R.id.toolbar)
        emptyState = findViewById(R.id.emptyState)
        scrollContent = findViewById(R.id.scrollContent)
        listAppointments = findViewById(R.id.listAppointments)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.appointments_title)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        storage = AppointmentStorage(this)
    }

    override fun onResume() {
        super.onResume()
        renderAppointments()
    }

    private fun renderAppointments() {
        val appointments = storage.getAppointments()
        listAppointments.removeAllViews()

        if (appointments.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            scrollContent.visibility = View.GONE
            return
        }

        emptyState.visibility = View.GONE
        scrollContent.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(this)
        appointments.forEach { appointment ->
            val itemView = inflater.inflate(R.layout.item_appointment, listAppointments, false)
            val tvPatientName = itemView.findViewById<TextView>(R.id.tvPatientName)
            val tvSpecialty = itemView.findViewById<TextView>(R.id.tvSpecialty)
            val tvDateTime = itemView.findViewById<TextView>(R.id.tvDateTime)
            val tvNotes = itemView.findViewById<TextView>(R.id.tvNotes)

            tvPatientName.text = getString(R.string.appointments_patient_name) +
                " " + appointment.patientName
            tvSpecialty.text = getString(R.string.appointments_specialty) +
                " " + appointment.specialty
            val dateTimeText = buildString {
                append(getString(R.string.appointments_date))
                append(' ')
                append(appointment.date)
                append(" • ")
                append(getString(R.string.appointments_time))
                append(' ')
                append(appointment.time)
            }
            tvDateTime.text = dateTimeText
            val notesText = if (appointment.notes.isBlank()) {
                getString(R.string.appointments_notes_empty)
            } else {
                getString(R.string.appointments_notes) + " " + appointment.notes
            }
            tvNotes.text = notesText

            listAppointments.addView(itemView)
        }
    }
}
