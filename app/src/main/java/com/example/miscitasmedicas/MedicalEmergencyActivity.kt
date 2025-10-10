package com.example.miscitasmedicas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.miscitasmedicas.databinding.ActivityMedicalEmergencyBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.widget.doAfterTextChanged

class MedicalEmergencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalEmergencyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.medical_emergency_title)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        configureDropdowns()
        configureFieldValidation()
        configureActions()
    }

    private fun configureDropdowns() {
        val areasAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.medical_emergency_areas)
        )
        binding.inputEmergencyArea.setAdapter(areasAdapter)
        binding.inputEmergencyArea.setOnItemClickListener { _, _, _, _ ->
            binding.layoutEmergencyArea.error = null
        }

        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.medical_emergency_priorities)
        )
        binding.inputEmergencyPriority.setAdapter(priorityAdapter)
        binding.inputEmergencyPriority.setOnItemClickListener { _, _, _, _ ->
            binding.layoutEmergencyPriority.error = null
        }
    }

    private fun configureFieldValidation() {
        binding.inputLocation.doAfterTextChanged {
            binding.layoutLocation.error = null
        }

        binding.inputContactNumber.doAfterTextChanged {
            binding.layoutContactNumber.error = null
        }
    }

    private fun configureActions() {
        binding.btnSubmitEmergency.setOnClickListener {
            val area = binding.inputEmergencyArea.text?.toString()?.trim().orEmpty()
            val priority = binding.inputEmergencyPriority.text?.toString()?.trim().orEmpty()
            val location = binding.inputLocation.text?.toString()?.trim().orEmpty()
            val contactNumber = binding.inputContactNumber.text?.toString()?.trim().orEmpty()
            val notes = binding.inputAdditionalNotes.text?.toString()?.trim().orEmpty()

            var isValid = true

            if (area.isEmpty()) {
                binding.layoutEmergencyArea.error = getString(R.string.medical_emergency_error_area)
                isValid = false
            } else {
                binding.layoutEmergencyArea.error = null
            }

            if (priority.isEmpty()) {
                binding.layoutEmergencyPriority.error = getString(R.string.medical_emergency_error_priority)
                isValid = false
            } else {
                binding.layoutEmergencyPriority.error = null
            }

            if (location.isEmpty()) {
                binding.layoutLocation.error = getString(R.string.medical_emergency_error_location)
                isValid = false
            } else {
                binding.layoutLocation.error = null
            }

            if (contactNumber.isEmpty()) {
                binding.layoutContactNumber.error = getString(R.string.medical_emergency_error_contact)
                isValid = false
            } else {
                binding.layoutContactNumber.error = null
            }

            if (!isValid) return@setOnClickListener

            val message = buildString {
                appendLine(getString(R.string.medical_emergency_summary_area, area))
                appendLine(getString(R.string.medical_emergency_summary_priority, priority))
                appendLine(getString(R.string.medical_emergency_summary_location, location))
                appendLine(getString(R.string.medical_emergency_summary_contact, contactNumber))
                if (notes.isNotEmpty()) {
                    append(getString(R.string.medical_emergency_summary_notes, notes))
                }
            }

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.medical_emergency_confirmation_title)
                .setMessage(message.trim())
                .setPositiveButton(R.string.medical_emergency_confirmation_positive) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnCallEmergency.setOnClickListener {
            val emergencyTrack = Uri.parse("https://open.spotify.com/track/3q7Ima7PlX2kb20zu0f8MU")
            val intent = Intent(Intent.ACTION_VIEW, emergencyTrack)
            startActivity(intent)
        }
    }
}
