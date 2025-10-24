package com.example.miscitasmedicas

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.miscitasmedicas.databinding.ActivityMedicalEmergencyBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MedicalEmergencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicalEmergencyBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var emergencySoundPlayer: MediaPlayer? = null
    private val locationCancellationToken = CancellationTokenSource()
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    R.string.medical_emergency_location_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private val emergencySoundResId: Int by lazy {
        val resourceName = getString(R.string.medical_emergency_sound_res_name)
            .removeSuffix(".mp3")
        val rawResId = resources.getIdentifier(resourceName, "raw", packageName)
        if (rawResId != 0) {
            rawResId
        } else {
            resources.getIdentifier(resourceName, "drawable", packageName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            playEmergencySound()
        }

        binding.btnUseCurrentLocation.setOnClickListener {
            fetchCurrentLocation()
        }
    }

    private fun playEmergencySound() {
        if (emergencySoundResId == 0) {
            Toast.makeText(
                this,
                R.string.medical_emergency_sound_not_found,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val player = emergencySoundPlayer ?: MediaPlayer.create(this, emergencySoundResId)
            .also { emergencySoundPlayer = it }

        player.seekTo(0)
        player.start()
    }

    private fun fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        binding.btnUseCurrentLocation.isEnabled = false
        binding.btnUseCurrentLocation.text = getString(R.string.medical_emergency_fetching_location)

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationToken.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val locationText = getString(
                        R.string.medical_emergency_location_format,
                        location.latitude,
                        location.longitude
                    )
                    binding.inputLocation.setText(locationText)
                    binding.inputLocation.setSelection(locationText.length)
                    binding.layoutLocation.error = null
                } else {
                    Toast.makeText(
                        this,
                        R.string.medical_emergency_location_unavailable,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    R.string.medical_emergency_location_unavailable,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnCompleteListener {
                restoreLocationButtonState()
            }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }

    private fun restoreLocationButtonState() {
        binding.btnUseCurrentLocation.isEnabled = true
        binding.btnUseCurrentLocation.text = getString(R.string.medical_emergency_use_current_location)
    }

    override fun onStop() {
        super.onStop()
        emergencySoundPlayer?.takeIf { it.isPlaying }?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCancellationToken.cancel()
        releaseEmergencySound()
    }

    private fun releaseEmergencySound() {
        emergencySoundPlayer?.release()
        emergencySoundPlayer = null
    }
}
