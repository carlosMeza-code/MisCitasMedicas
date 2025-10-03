package com.example.miscitasmedicas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.example.miscitasmedicas.databinding.ItemAppointmentBinding

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
            val itemBinding = ItemAppointmentBinding.inflate(inflater, listAppointments, false)
            itemBinding.tvPatientName.text = getString(
                R.string.appointments_patient_name,
                appointment.patientName
            )
            itemBinding.tvSpecialty.text = getString(
                R.string.appointments_specialty,
                appointment.specialty
            )
            itemBinding.tvDateTime.text = getString(
                R.string.appointments_date_time,
                appointment.date,
                appointment.time
            )
            val notesText = if (appointment.notes.isBlank()) {
                getString(R.string.appointments_notes_empty)
            } else {
                getString(R.string.appointments_notes, appointment.notes)
            }
            itemBinding.tvNotes.text = notesText

            listAppointments.addView(itemBinding.root)
        }
    }
}
