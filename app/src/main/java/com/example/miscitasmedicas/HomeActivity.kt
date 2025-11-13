package com.example.miscitasmedicas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlin.LazyThreadSafetyMode

class HomeActivity : AppCompatActivity() {

    private val reminderPreferences by lazy(LazyThreadSafetyMode.NONE) {
        ReminderPreferences(applicationContext)
    }
    private val sessionManager by lazy(LazyThreadSafetyMode.NONE) {
        SessionManager(applicationContext)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startReminderService()
            } else {
                reminderPreferences.setEnabled(false)
                Toast.makeText(
                    this,
                    getString(R.string.reminder_toast_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
                invalidateOptionsMenu()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: MaterialToolbar = findViewById(R.id.homeToolbar)
        setSupportActionBar(toolbar)

        findViewById<MaterialButton>(R.id.btnToSpecialties).setOnClickListener {
            startActivity(Intent(this, SpecialtiesActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnToDoctors).setOnClickListener {
            startActivity(Intent(this, DoctorsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnNewAppointment).setOnClickListener {
            startActivity(Intent(this, NewAppointmentActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnAppointments).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnEmergencyCall).setOnClickListener {
            startActivity(Intent(this, MedicalEmergencyActivity::class.java))
        }

        if (reminderPreferences.isEnabled()) {
            ensureReminderService()
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val toggleItem = menu.findItem(R.id.action_toggle_reminders)
        toggleItem?.title = if (AppointmentReminderService.isRunning()) {
            getString(R.string.menu_home_disable_reminders)
        } else {
            getString(R.string.menu_home_enable_reminders)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_view_appointments -> {
                startActivity(Intent(this, AppointmentsActivity::class.java))
                true
            }

            R.id.action_toggle_reminders -> {
                toggleReminderService()
                true
            }

            R.id.action_logout -> {
                logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleReminderService() {
        val running = AppointmentReminderService.isRunning()
        if (running) {
            reminderPreferences.setEnabled(false)
            stopService(Intent(this, AppointmentReminderService::class.java))
            Toast.makeText(this, getString(R.string.reminder_toast_stopped), Toast.LENGTH_SHORT)
                .show()
        } else {
            reminderPreferences.setEnabled(true)
            ensureReminderService()
        }
        invalidateOptionsMenu()
    }

    private fun ensureReminderService() {
        if (AppointmentReminderService.isRunning()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startReminderService()
        }
    }

    private fun startReminderService() {
        if (!AppointmentReminderService.isRunning()) {
            ContextCompat.startForegroundService(
                this,
                Intent(this, AppointmentReminderService::class.java)
            )
            Toast.makeText(this, getString(R.string.reminder_toast_started), Toast.LENGTH_SHORT)
                .show()
        }
        invalidateOptionsMenu()
    }

    private fun logout() {
        reminderPreferences.setEnabled(false)
        stopService(Intent(this, AppointmentReminderService::class.java))
        sessionManager.clear()

        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }
}
