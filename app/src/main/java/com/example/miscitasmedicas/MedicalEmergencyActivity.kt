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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MedicalEmergencyActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMedicalEmergencyBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var emergencySoundPlayer: MediaPlayer? = null
    private var locationCancellationToken: CancellationTokenSource? = null
    private var googleMap: GoogleMap? = null
    private var lastKnownLatLng: LatLng? = null
    private var lastLocationRequestUserInitiated = false
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                enableMyLocationLayer()
                fetchCurrentLocation(lastLocationRequestUserInitiated)
            } else {
                if (lastLocationRequestUserInitiated) {
                    restoreLocationButtonState()
                }
                Toast.makeText(
                    this,
                    R.string.medical_emergency_location_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
            lastLocationRequestUserInitiated = false
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
        initializeMap(savedInstanceState)
        configureActions()
        fetchCurrentLocation(userInitiated = false)
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

    private fun fetchCurrentLocation(userInitiated: Boolean = true) {
        if (!hasLocationPermission()) {
            lastLocationRequestUserInitiated = userInitiated
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        lastLocationRequestUserInitiated = false

        if (userInitiated) {
            binding.btnUseCurrentLocation.isEnabled = false
            binding.btnUseCurrentLocation.text = getString(R.string.medical_emergency_fetching_location)
        }

        locationCancellationToken?.cancel()
        val newCancellationToken = CancellationTokenSource().also {
            locationCancellationToken = it
        }

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, newCancellationToken.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val locationText = getString(
                        R.string.medical_emergency_location_format,
                        latitude,
                        longitude
                    )
                    binding.inputLocation.setText(locationText)
                    binding.inputLocation.setSelection(locationText.length)
                    binding.layoutLocation.error = null
                    showLocationOnMap(LatLng(latitude, longitude))
                    enableMyLocationLayer()
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
                if (userInitiated) {
                    restoreLocationButtonState()
                }
                if (locationCancellationToken == newCancellationToken) {
                    locationCancellationToken = null
                }
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

    private fun initializeMap(savedInstanceState: Bundle?) {
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        binding.mapEmergencyLocation.onCreate(mapViewBundle)
        binding.mapEmergencyLocation.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMapToolbarEnabled = false
        }
        enableMyLocationLayer()
        lastKnownLatLng?.let { showLocationOnMap(it) }
    }

    private fun showLocationOnMap(latLng: LatLng) {
        lastKnownLatLng = latLng
        googleMap?.let { map ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.medical_emergency_map_marker_title))
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM))
        }
    }

    private fun enableMyLocationLayer() {
        if (!hasLocationPermission()) return
        googleMap?.let { map ->
            try {
                map.isMyLocationEnabled = true
            } catch (_: SecurityException) {
                // Ignore the exception since permission was checked before enabling the layer
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.mapEmergencyLocation.onStop()
        emergencySoundPlayer?.takeIf { it.isPlaying }?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapEmergencyLocation.onDestroy()
        locationCancellationToken?.cancel()
        locationCancellationToken = null
        releaseEmergencySound()
    }

    override fun onStart() {
        super.onStart()
        binding.mapEmergencyLocation.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapEmergencyLocation.onResume()
    }

    override fun onPause() {
        binding.mapEmergencyLocation.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapEmergencyLocation.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY) ?: Bundle().also {
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, it)
        }
        binding.mapEmergencyLocation.onSaveInstanceState(mapViewBundle)
    }

    private fun releaseEmergencySound() {
        emergencySoundPlayer?.release()
        emergencySoundPlayer = null
    }

    companion object {
        private const val MAP_VIEW_BUNDLE_KEY = "medicalEmergencyMapViewBundle"
        private const val DEFAULT_MAP_ZOOM = 16f
    }
}
