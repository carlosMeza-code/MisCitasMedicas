package com.example.miscitasmedicas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
            startActivity(Intent(this, NewAppointmentActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnAppointments).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnEmergencyCall).setOnClickListener {
            val emergencyPlaylist = Uri.parse("https://open.spotify.com/track/47cVjB6QKLkV8RgzGLNJOU?si=R2KobkRcQwKP2TgAq1B5Dw")
            val intent = Intent(Intent.ACTION_VIEW, emergencyPlaylist)
            startActivity(intent)
        }
    }
}
