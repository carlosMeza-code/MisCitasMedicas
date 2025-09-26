package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<MaterialButton>(R.id.btnToSpecialties).setOnClickListener {
            startActivity(Intent(this, SpecialtiesActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnToDoctors).setOnClickListener {
            startActivity(Intent(this, DoctorsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnNewAppointment).setOnClickListener {
            Toast.makeText(this, R.string.placeholder_new_appointment, Toast.LENGTH_SHORT).show()
        }
    }
}
