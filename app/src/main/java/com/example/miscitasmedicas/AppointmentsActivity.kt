package com.example.miscitasmedicas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.miscitasmedicas.databinding.ActivityAppointmentsBinding
import com.example.miscitasmedicas.databinding.ItemAppointmentBinding

class AppointmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentsBinding
    private lateinit var storage: AppointmentStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.appointments_title)
        binding.toolbar.setNavigationOnClickListener {
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
        binding.listAppointments.removeAllViews()

        if (appointments.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.scrollContent.visibility = View.GONE
            return
        }

        binding.emptyState.visibility = View.GONE
        binding.scrollContent.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(this)
        appointments.forEach { appointment ->
            val itemBinding = ItemAppointmentBinding.inflate(inflater, binding.listAppointments, false)
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

            binding.listAppointments.addView(itemBinding.root)
        }
    }
}
