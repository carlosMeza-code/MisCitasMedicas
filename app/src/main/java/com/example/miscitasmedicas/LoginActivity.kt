package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoRegister: MaterialButton
    private lateinit var loadingOverlay: View
    private lateinit var overlayLogo: ImageView
    private lateinit var sessionManager: SessionManager
    private lateinit var pulseAnimation: Animation
    private val handler = Handler(Looper.getMainLooper())
    private var pendingNavigation: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        bindViews()
        setupListeners()
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
    }

    private fun bindViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
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
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_email_required)
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.error_email_invalid)
            hasError = true
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_required)
            hasError = true
        }

        if (hasError) return

        val user = sessionManager.getUser()
        if (user == null) {
            Toast.makeText(this, R.string.error_user_not_found, Toast.LENGTH_LONG).show()
            return
        }

        if (email.equals(user.email, ignoreCase = true) && password == user.password) {
            showLoadingOverlay()
            btnLogin.isEnabled = false
            pendingNavigation = Runnable {
                Toast.makeText(this, getString(R.string.success_login, user.name), Toast.LENGTH_SHORT).show()
                hideLoadingOverlay()
                btnLogin.isEnabled = true
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }.also { handler.postDelayed(it, 1200) }
        } else {
            tilPassword.error = getString(R.string.error_credentials)
        }
    }

    private fun clearErrors() {
        tilEmail.error = null
        tilPassword.error = null
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
