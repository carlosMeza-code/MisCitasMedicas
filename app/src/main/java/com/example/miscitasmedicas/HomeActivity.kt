package com.example.miscitasmedicas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
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
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: MaterialToolbar = findViewById(R.id.homeToolbar)
        setSupportActionBar(toolbar)

        bindHomeShortcuts()

        if (reminderPreferences.isEnabled()) {
            ensureReminderService()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_menu -> {
                showHomeMenu()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bindHomeShortcuts() {
        findViewById<MaterialCardView>(R.id.cardInPerson).setOnClickListener {
            startActivity(Intent(this, SpecialtiesActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardVirtual).setOnClickListener {
            startActivity(Intent(this, DoctorsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardHomeService).setOnClickListener {
            startActivity(Intent(this, MedicalEmergencyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardHistory).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardPending).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardLocations).setOnClickListener {
            showComingSoon()
        }

        findViewById<MaterialButton>(R.id.btnEmergencyCall).setOnClickListener {
            startActivity(Intent(this, MedicalEmergencyActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnCampaignAction).setOnClickListener {
            showComingSoon()
        }

        findViewById<ExtendedFloatingActionButton>(R.id.fabNewAppointment).setOnClickListener {
            startActivity(Intent(this, NewAppointmentActivity::class.java))
        }

        findViewById<TextInputEditText>(R.id.etHomeSearch).setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text?.toString()?.trim().orEmpty()
                if (query.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.home_option_soon),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun showHomeMenu() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_home_menu_bottom_sheet, null)
        dialog.setContentView(view)

        val reminderStatus = view.findViewById<TextView>(R.id.tvReminderStatus)
        updateReminderStatus(reminderStatus)

        view.findViewById<MaterialCardView>(R.id.menuInPerson)?.setOnClickListener {
            startActivity(Intent(this, SpecialtiesActivity::class.java))
            dialog.dismiss()
        }

        view.findViewById<MaterialCardView>(R.id.menuVirtual)?.setOnClickListener {
            startActivity(Intent(this, DoctorsActivity::class.java))
            dialog.dismiss()
        }

        view.findViewById<MaterialCardView>(R.id.menuHomeService)?.setOnClickListener {
            startActivity(Intent(this, MedicalEmergencyActivity::class.java))
            dialog.dismiss()
        }

        view.findViewById<MaterialCardView>(R.id.menuHistory)?.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
            dialog.dismiss()
        }

        view.findViewById<MaterialCardView>(R.id.menuPending)?.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
            dialog.dismiss()
        }

        view.findViewById<MaterialCardView>(R.id.menuLocations)?.setOnClickListener {
            showComingSoon()
        }

        view.findViewById<MaterialCardView>(R.id.menuSupport)?.setOnClickListener {
            showComingSoon()
        }

        view.findViewById<MaterialCardView>(R.id.menuReminders)?.setOnClickListener {
            toggleReminderService()
            updateReminderStatus(reminderStatus)
        }

        view.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            Toast.makeText(this, getString(R.string.home_option_soon), Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            dialog.dismiss()
            logout()
        }

        dialog.show()
    }

    private fun updateReminderStatus(textView: TextView?) {
        val running = AppointmentReminderService.isRunning()
        textView?.text = if (running) {
            getString(R.string.home_reminders_on)
        } else {
            getString(R.string.home_reminders_off)
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
    }

    private fun ensureReminderService() {
        if (AppointmentReminderService.isRunning()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
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
    }

    private fun showComingSoon() {
        Toast.makeText(this, getString(R.string.home_option_soon), Toast.LENGTH_SHORT).show()
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
