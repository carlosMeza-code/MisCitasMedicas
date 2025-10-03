package com.example.miscitasmedicas

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.miscitasmedicas.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.LazyThreadSafetyMode

class NewAppointmentFragment : Fragment() {

    private var hostContext: Context? = null

    private val calendar: Calendar = Calendar.getInstance()
    private val appointmentStorage: AppointmentStorage by lazy(LazyThreadSafetyMode.NONE) {
        AppointmentStorage(requireContext().applicationContext)
    }

    private var ui: UiElements? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hostContext = context
        logLifecycle("onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycle("onCreate")
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logLifecycle("onCreateView")
        val view = inflater.inflate(R.layout.fragment_new_appointment, container, false)
        ui = UiElements(
            textFieldPatientName = view.findViewById(R.id.textFieldPatientName),
            inputPatientName = view.findViewById(R.id.inputPatientName),
            textFieldSpecialty = view.findViewById(R.id.textFieldSpecialty),
            inputSpecialty = view.findViewById(R.id.inputSpecialty),
            textFieldAppointmentDate = view.findViewById(R.id.textFieldAppointmentDate),
            inputAppointmentDate = view.findViewById(R.id.inputAppointmentDate),
            textFieldAppointmentTime = view.findViewById(R.id.textFieldAppointmentTime),
            inputAppointmentTime = view.findViewById(R.id.inputAppointmentTime),
            inputNotes = view.findViewById(R.id.inputNotes),
            btnSchedule = view.findViewById(R.id.btnSchedule),
            cardConfirmation = view.findViewById(R.id.cardConfirmation),
            tvConfirmation = view.findViewById(R.id.tvConfirmation)
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logLifecycle("onViewCreated")
        setupUi()
    }

    override fun onStart() {
        super.onStart()
        logLifecycle("onStart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle("onResume")
    }

    override fun onPause() {
        logLifecycle("onPause")
        super.onPause()
    }

    override fun onStop() {
        logLifecycle("onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        logLifecycle("onDestroyView")
        super.onDestroyView()
        ui = null
    }

    override fun onDestroy() {
        logLifecycle("onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        logLifecycle("onDetach")
        hostContext = null
        super.onDetach()
    }

    private fun setupUi() {
        val ui = ui ?: return
        val specialties = resources.getStringArray(R.array.new_appointment_specialties)
        val adapter = ArrayAdapter(
            hostContext ?: requireContext(),
            android.R.layout.simple_list_item_1,
            specialties
        )
        ui.inputSpecialty.setAdapter(adapter)
        ui.inputSpecialty.setOnItemClickListener { _, _, _, _ ->
            ui.textFieldSpecialty.error = null
        }

        ui.inputAppointmentDate.setOnClickListener { showDatePicker() }
        ui.inputAppointmentDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker()
        }

        ui.inputAppointmentTime.setOnClickListener { showTimePicker() }
        ui.inputAppointmentTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showTimePicker()
        }

        ui.btnSchedule.setOnClickListener { validateAndSchedule() }
    }

    private fun showDatePicker() {
        val ui = ui ?: return
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.new_appointment_date_picker_title))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = selection

            calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))

            val formatter = SimpleDateFormat("dd 'de' MMMM yyyy", Locale.getDefault())
            ui.inputAppointmentDate.setText(formatter.format(calendar.time))
            ui.textFieldAppointmentDate.error = null
        }

        picker.show(parentFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker() {
        val ui = ui ?: return
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(getString(R.string.new_appointment_time_picker_title))
            .build()

        picker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, picker.hour)
            calendar.set(Calendar.MINUTE, picker.minute)

            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            ui.inputAppointmentTime.setText(formatter.format(calendar.time))
            ui.textFieldAppointmentTime.error = null
        }

        picker.show(parentFragmentManager, TIME_PICKER_TAG)
    }

    private fun validateAndSchedule() {
        val ui = ui ?: return
        val name = ui.inputPatientName.text?.toString().orEmpty().trim()
        val specialty = ui.inputSpecialty.text?.toString().orEmpty().trim()
        val date = ui.inputAppointmentDate.text?.toString().orEmpty().trim()
        val time = ui.inputAppointmentTime.text?.toString().orEmpty().trim()
        val notes = ui.inputNotes.text?.toString().orEmpty().trim()

        ui.textFieldPatientName.error = if (name.isEmpty()) {
            getString(R.string.error_name_required)
        } else {
            null
        }

        ui.textFieldSpecialty.error = if (specialty.isEmpty()) {
            getString(R.string.new_appointment_error_specialty)
        } else {
            null
        }

        ui.textFieldAppointmentDate.error = if (date.isEmpty()) {
            getString(R.string.new_appointment_error_date)
        } else {
            null
        }

        ui.textFieldAppointmentTime.error = if (time.isEmpty()) {
            getString(R.string.new_appointment_error_time)
        } else {
            null
        }

        val hasError = listOf(
            ui.textFieldPatientName.error,
            ui.textFieldSpecialty.error,
            ui.textFieldAppointmentDate.error,
            ui.textFieldAppointmentTime.error
        ).any { it != null }

        if (hasError) {
            Toast.makeText(
                hostContext ?: requireContext(),
                getString(R.string.new_appointment_error_form),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val summary = getString(
            R.string.new_appointment_confirmation,
            name,
            specialty,
            date,
            time,
            if (notes.isEmpty()) getString(R.string.new_appointment_no_notes) else notes
        )

        appointmentStorage.saveAppointment(
            Appointment(
                patientName = name,
                specialty = specialty,
                date = date,
                time = time,
                notes = notes
            )
        )

        ui.cardConfirmation.visibility = View.VISIBLE
        ui.tvConfirmation.text = summary
        ui.cardConfirmation.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.medical_primary_container)
        )

        Toast.makeText(
            hostContext ?: requireContext(),
            getString(R.string.new_appointment_success_toast),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun logLifecycle(event: String) {
        Log.d(TAG, "🔁 $event — Fragment de nueva cita activo")
    }

    private data class UiElements(
        val textFieldPatientName: TextInputLayout,
        val inputPatientName: TextInputEditText,
        val textFieldSpecialty: TextInputLayout,
        val inputSpecialty: MaterialAutoCompleteTextView,
        val textFieldAppointmentDate: TextInputLayout,
        val inputAppointmentDate: TextInputEditText,
        val textFieldAppointmentTime: TextInputLayout,
        val inputAppointmentTime: TextInputEditText,
        val inputNotes: TextInputEditText,
        val btnSchedule: MaterialButton,
        val cardConfirmation: MaterialCardView,
        val tvConfirmation: TextView
    )

    companion object {
        private const val TAG = "NewAppointmentFragment"
        private const val DATE_PICKER_TAG = "NewAppointmentDatePicker"
        private const val TIME_PICKER_TAG = "NewAppointmentTimePicker"

        fun newInstance(): NewAppointmentFragment = NewAppointmentFragment()
    }
}
