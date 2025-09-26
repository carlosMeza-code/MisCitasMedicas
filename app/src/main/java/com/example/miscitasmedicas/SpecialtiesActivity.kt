package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class SpecialtiesActivity : AppCompatActivity() {

    private lateinit var adapter: SpecialtyAdapter
    private lateinit var specialties: List<Specialty>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.mtrl_ic_arrow_back)
        toolbar.setNavigationOnClickListener { finish() }

        specialties = defaultSpecialties()
        adapter = SpecialtyAdapter(specialties) { specialty ->
            val intent = Intent(this, DoctorsActivity::class.java)
            intent.putExtra(DoctorsActivity.EXTRA_SPECIALTY_ID, specialty.id)
            intent.putExtra(DoctorsActivity.EXTRA_SPECIALTY_NAME, specialty.name)
            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.rvSpecialties).adapter = adapter
        val searchEditText = findViewById<TextInputEditText>(R.id.etSearch)

        searchEditText.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            filterSpecialties(query)
        }
    }

    private fun filterSpecialties(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(specialties)
            return
        }

        val filtered = specialties.filter {
            it.name.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }
}
