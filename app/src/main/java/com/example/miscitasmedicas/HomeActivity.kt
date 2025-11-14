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
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlin.LazyThreadSafetyMode
import java.util.Locale

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

        setupGreeting()
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
            showLocationsDialog()
        }

        findViewById<MaterialButton>(R.id.btnEmergencyCall).setOnClickListener {
            startActivity(Intent(this, MedicalEmergencyActivity::class.java))
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

        findViewById<Chip>(R.id.chipCalendar).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        findViewById<Chip>(R.id.chipPending).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        findViewById<Chip>(R.id.chipPromotions).setOnClickListener {
            showBenefitDetails(R.string.home_benefits_promotions)
        }

        findViewById<Chip>(R.id.chipDiscounts).setOnClickListener {
            showBenefitDetails(R.string.home_benefits_discounts)
        }

        findViewById<Chip>(R.id.chipLocations).setOnClickListener {
            showLocationsDialog()
        }

        findViewById<Chip>(R.id.chipReminders).apply {
            updateReminderStatus(chip = this)
            setOnClickListener {
                toggleReminderService()
                updateReminderStatus(chip = this)
            }
        }

        findViewById<MaterialButton>(R.id.btnSupport).setOnClickListener {
            showSupportDialog()
        }

        findViewById<MaterialButton>(R.id.btnTerms).setOnClickListener {
            showTermsDialog()
        }

        findViewById<MaterialButton>(R.id.btnDeleteAccount).setOnClickListener {
            confirmAccountDeletion()
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            logout()
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
            showLocationsDialog()
        }

        view.findViewById<MaterialCardView>(R.id.menuSupport)?.setOnClickListener {
            showSupportDialog()
        }

        view.findViewById<MaterialCardView>(R.id.menuReminders)?.setOnClickListener {
            toggleReminderService()
            updateReminderStatus(reminderStatus)
            updateReminderStatus(chip = findViewById(R.id.chipReminders))
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

    private fun updateReminderStatus(textView: TextView? = null, chip: Chip? = null) {
        val running = AppointmentReminderService.isRunning()
        val statusText = if (running) {
            getString(R.string.home_reminders_on)
        } else {
            getString(R.string.home_reminders_off)
        }
        textView?.text = statusText
        chip?.text = statusText
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

    private fun setupGreeting() {
        val greetingView = findViewById<TextView>(R.id.tvHomeTitle)
        val subtitleView = findViewById<TextView>(R.id.tvHomeSubtitle)
        val displayName = sessionManager.getUser()?.name
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.split(" ")
            ?.firstOrNull()
            ?.lowercase(Locale.getDefault())
            ?.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase(Locale.getDefault())
                } else {
                    char.toString()
                }
            }

        greetingView.text = displayName?.let { getString(R.string.home_greeting, it) }
            ?: getString(R.string.home_title)
        subtitleView.text = getString(R.string.home_subtitle)
    }

    private fun showLocationsDialog() {
        val locations = resources.getStringArray(R.array.home_locations)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.home_locations_dialog_title)
            .setItems(locations, null)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showSupportDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.home_support_title)
            .setMessage(R.string.home_support_message)
            .setPositiveButton(R.string.home_support_positive, null)
            .show()
    }

    private fun showTermsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.home_terms_title)
            .setMessage(R.string.home_terms_message)
            .setPositiveButton(R.string.home_support_positive, null)
            .show()
    }

    private fun showBenefitDetails(messageRes: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.home_benefits_title)
            .setMessage(messageRes)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun confirmAccountDeletion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.home_delete_account_title)
            .setMessage(R.string.home_delete_account_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.home_delete_account_positive) { _, _ ->
                Toast.makeText(this, getString(R.string.home_delete_account_toast), Toast.LENGTH_SHORT)
                    .show()
                logout()
            }
            .show()
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
