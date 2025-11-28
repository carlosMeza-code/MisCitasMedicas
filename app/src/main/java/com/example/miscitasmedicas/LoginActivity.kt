package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth   // <--- NUEVO

class LoginActivity : AppCompatActivity() {

    private lateinit var tilDocument: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etDocument: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var loadingOverlay: View
    private lateinit var overlayLogo: ImageView
    private lateinit var sessionManager: SessionManager
    private lateinit var pulseAnimation: Animation
    private val handler = Handler(Looper.getMainLooper())
    private var pendingNavigation: Runnable? = null

    private lateinit var auth: FirebaseAuth   // <--- NUEVO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        auth = FirebaseAuth.getInstance()     // <--- NUEVO

        bindViews()
        setupListeners()
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
    }

    private fun bindViews() {
        tilDocument = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etDocument = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        overlayLogo = findViewById(R.id.ivOverlayLogo)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { handleLogin() }
        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
        pendingNavigation?.let { handler.removeCallbacks(it) }
        clearErrors()

        val document = etDocument.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (document.isEmpty()) {
            tilDocument.error = getString(R.string.error_email_required)
            hasError = true
        } else if (!isValidDocument(document)) {
            tilDocument.error = getString(R.string.error_email_invalid)
            hasError = true
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_required)
            hasError = true
        }

        if (hasError) return

        // ------------- LOGIN CON FIREBASE ----------------
        val emailForFirebase = "$document@dni.miscitasmedicas.com"

        showLoadingOverlay()
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(emailForFirebase, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val localUser = sessionManager.getUser()

                    pendingNavigation = Runnable {
                        // Si tenemos nombre guardado, lo usamos; si no, mostramos el documento
                        val displayName = localUser?.name ?: document
                        Toast.makeText(
                            this,
                            getString(R.string.success_login, displayName),
                            Toast.LENGTH_SHORT
                        ).show()
                        hideLoadingOverlay()
                        btnLogin.isEnabled = true
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }.also { handler.postDelayed(it, 1200) }
                } else {
                    hideLoadingOverlay()
                    btnLogin.isEnabled = true
                    tilPassword.error = getString(R.string.error_credentials)

                    // Si quieres ver el error real para depurar:
                    // Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun clearErrors() {
        tilDocument.error = null
        tilPassword.error = null
    }

    private fun isValidDocument(document: String): Boolean {
        if (document.length !in 8..12) {
            return false
        }
        return document.all { it.isLetterOrDigit() }
    }

    private fun showLoadingOverlay() {
        loadingOverlay.visibility = View.VISIBLE
        overlayLogo.startAnimation(pulseAnimation)
    }

    private fun hideLoadingOverlay() {
        overlayLogo.clearAnimation()
        loadingOverlay.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingNavigation?.let { handler.removeCallbacks(it) }
    }
}
