package com.example.miscitasmedicas

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.miscitasmedicas.databinding.FragmentNewAppointmentBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NewAppointmentFragment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null
    private val binding get() = _binding!!
    private var hostContext: Context? = null

    private val calendar: Calendar = Calendar.getInstance()

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
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        return binding.root
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
        _binding = null
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
        val specialties = resources.getStringArray(R.array.new_appointment_specialties)
        val adapter = ArrayAdapter(
            hostContext ?: requireContext(),
            android.R.layout.simple_list_item_1,
            specialties
        )
        binding.inputSpecialty.setAdapter(adapter)
        binding.inputSpecialty.setOnItemClickListener { _, _, _, _ ->
            binding.textFieldSpecialty.error = null
        }

        binding.inputAppointmentDate.setOnClickListener { showDatePicker() }
        binding.inputAppointmentDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker()
        }

        binding.inputAppointmentTime.setOnClickListener { showTimePicker() }
        binding.inputAppointmentTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showTimePicker()
        }

        binding.btnSchedule.setOnClickListener { validateAndSchedule() }
    }

    private fun showDatePicker() {
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
            binding.inputAppointmentDate.setText(formatter.format(calendar.time))
            binding.textFieldAppointmentDate.error = null
        }

        picker.show(parentFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker() {
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
            binding.inputAppointmentTime.setText(formatter.format(calendar.time))
            binding.textFieldAppointmentTime.error = null
        }

        picker.show(parentFragmentManager, TIME_PICKER_TAG)
    }

    private fun validateAndSchedule() {
        val name = binding.inputPatientName.text?.toString().orEmpty().trim()
        val specialty = binding.inputSpecialty.text?.toString().orEmpty().trim()
        val date = binding.inputAppointmentDate.text?.toString().orEmpty().trim()
        val time = binding.inputAppointmentTime.text?.toString().orEmpty().trim()
        val notes = binding.inputNotes.text?.toString().orEmpty().trim()

        binding.textFieldPatientName.error = if (name.isEmpty()) {
            getString(R.string.error_name_required)
        } else {
            null
        }

        binding.textFieldSpecialty.error = if (specialty.isEmpty()) {
            getString(R.string.new_appointment_error_specialty)
        } else {
            null
        }

        binding.textFieldAppointmentDate.error = if (date.isEmpty()) {
            getString(R.string.new_appointment_error_date)
        } else {
            null
        }

        binding.textFieldAppointmentTime.error = if (time.isEmpty()) {
            getString(R.string.new_appointment_error_time)
        } else {
            null
        }

        val hasError = listOf(
            binding.textFieldPatientName.error,
            binding.textFieldSpecialty.error,
            binding.textFieldAppointmentDate.error,
            binding.textFieldAppointmentTime.error
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

        binding.cardConfirmation.visibility = View.VISIBLE
        binding.tvConfirmation.text = summary
        binding.cardConfirmation.setCardBackgroundColor(
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

    companion object {
        private const val TAG = "NewAppointmentFragment"
        private const val DATE_PICKER_TAG = "NewAppointmentDatePicker"
        private const val TIME_PICKER_TAG = "NewAppointmentTimePicker"

        fun newInstance(): NewAppointmentFragment = NewAppointmentFragment()
    }
}
