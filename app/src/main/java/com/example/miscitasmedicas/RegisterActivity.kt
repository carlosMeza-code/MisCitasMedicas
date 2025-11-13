package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilDocument: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilPasswordRepeat: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etDocument: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPasswordRepeat: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnGoLogin: MaterialButton
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)
        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        tilName = findViewById(R.id.tilName)
        tilDocument = findViewById(R.id.tilEmailReg)
        tilPassword = findViewById(R.id.tilPassReg)
        tilPasswordRepeat = findViewById(R.id.tilPass2Reg)
        etName = findViewById(R.id.etName)
        etDocument = findViewById(R.id.etEmailReg)
        etPassword = findViewById(R.id.etPassReg)
        etPasswordRepeat = findViewById(R.id.etPass2Reg)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoLogin = findViewById(R.id.btnGoLogin)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener { handleRegister() }
        btnGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun handleRegister() {
        clearErrors()

        val name = etName.text?.toString()?.trim().orEmpty()
        val document = etDocument.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString()?.trim().orEmpty()
        val passwordRepeat = etPasswordRepeat.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (name.isEmpty()) {
            tilName.error = getString(R.string.error_name_required)
            hasError = true
        }

        if (document.isEmpty()) {
            tilDocument.error = getString(R.string.error_email_required)
            hasError = true
        } else if (!isValidDocument(document)) {
            tilDocument.error = getString(R.string.error_email_invalid)
            hasError = true
        }

        if (password.length < 6) {
            tilPassword.error = getString(R.string.error_password_length)
            hasError = true
        }

        if (passwordRepeat != password) {
            tilPasswordRepeat.error = getString(R.string.error_password_mismatch)
            hasError = true
        }

        if (hasError) return

        val user = User(name = name, document = document, password = password)
        sessionManager.saveUser(user)

        Toast.makeText(this, R.string.success_register, Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun clearErrors() {
        tilName.error = null
        tilDocument.error = null
        tilPassword.error = null
        tilPasswordRepeat.error = null
    }

    private fun isValidDocument(document: String): Boolean {
        if (document.length !in 8..12) {
            return false
        }
        return document.all { it.isLetterOrDigit() }
    }
}
