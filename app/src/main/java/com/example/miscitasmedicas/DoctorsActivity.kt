package com.example.miscitasmedicas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class DoctorsActivity : AppCompatActivity() {

    private lateinit var adapter: DoctorAdapter
    private lateinit var doctors: List<Doctor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctors)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { finish() }

        val specialtyName = intent.getStringExtra(EXTRA_SPECIALTY_NAME)
        val header = findViewById<MaterialTextView>(R.id.tvSpecialtyHeader)
        if (!specialtyName.isNullOrBlank()) {
            header.text = getString(R.string.doctors_of, specialtyName)
        }

        val selectedSpecialtyId = intent.getStringExtra(EXTRA_SPECIALTY_ID)
        doctors = if (selectedSpecialtyId.isNullOrBlank()) {
            defaultDoctors()
        } else {
            defaultDoctors().filter { it.specialtyId == selectedSpecialtyId }
        }

        adapter = DoctorAdapter(doctors)
        findViewById<RecyclerView>(R.id.rvDoctors).adapter = adapter

        val searchEditText = findViewById<TextInputEditText>(R.id.etSearchDoc)
        searchEditText.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            filterDoctors(query)
        }
    }

    private fun filterDoctors(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(doctors)
            return
        }

        val filtered = doctors.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.cmp.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }

    companion object {
        const val EXTRA_SPECIALTY_ID = "extra_specialty_id"
        const val EXTRA_SPECIALTY_NAME = "extra_specialty_name"
    }
}
