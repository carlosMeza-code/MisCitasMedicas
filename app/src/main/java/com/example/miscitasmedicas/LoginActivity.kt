package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { handleLogin() }
        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
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
            Toast.makeText(this, getString(R.string.success_login, user.name), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            tilPassword.error = getString(R.string.error_credentials)
        }
    }

    private fun clearErrors() {
        tilEmail.error = null
        tilPassword.error = null
    }
}
