package com.example.miscitasmedicas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener

class ContactsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val realtimeManager by lazy { RealtimeManager() }
    private val contactsAdapter = ContactsAdapter()
    private var contactsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.contactsToolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvContacts)
        val emptyView = findViewById<android.widget.TextView>(R.id.tvContactsEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = contactsAdapter

        findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.fabAddContact)
            .setOnClickListener { showNewContactDialog(user.uid) }

        realtimeManager.ensureDefaultContacts()
        contactsListener = realtimeManager.listenToContacts(
            onResult = { contacts ->
                contactsAdapter.submitList(contacts.sortedBy { it.fullName })
                emptyView.visibility = if (contacts.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            },
            onError = { error ->
                Toast.makeText(
                    this,
                    getString(R.string.contacts_error_loading, error.message ?: "Error"),
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        contactsListener?.let { listener ->
            realtimeManager.removeContactsListener(listener)
        }
    }

    private fun showNewContactDialog(userId: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_new_contact, null)
        val etName: TextInputEditText = view.findViewById(R.id.etContactName)
        val etRelationship: TextInputEditText = view.findViewById(R.id.etContactRelationship)
        val etPhone: TextInputEditText = view.findViewById(R.id.etContactPhone)
        val etEmail: TextInputEditText = view.findViewById(R.id.etContactEmail)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.contacts_dialog_title)
            .setView(view)
            .setPositiveButton(R.string.contacts_action_save) { dialog, _ ->
                val name = etName.text?.toString()?.trim().orEmpty()
                val relationship = etRelationship.text?.toString()?.trim().orEmpty()
                val phone = etPhone.text?.toString()?.trim().orEmpty()
                val email = etEmail.text?.toString()?.trim().orEmpty()

                if (name.isBlank() || phone.isBlank()) {
                    Toast.makeText(
                        this,
                        getString(R.string.contacts_error_required_fields),
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.dismiss()
                    return@setPositiveButton
                }

                val contact = Contact(
                    fullName = name,
                    relationship = relationship,
                    phone = phone,
                    email = email
                )

                realtimeManager.addContact(contact) { success ->
                    val message = if (success) {
                        getString(R.string.contacts_success_saved)
                    } else {
                        getString(R.string.contacts_error_saving)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
