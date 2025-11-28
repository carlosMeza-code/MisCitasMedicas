package com.example.miscitasmedicas

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val NODE_USERS = "users"
private const val NODE_CONTACTS = "contacts"

/**
 * Maneja la interacción con Firebase Realtime Database para contactos.
 */
class RealtimeManager {

    private val database = FirebaseDatabase.getInstance().reference

    private fun contactsRef(userId: String) =
        database.child(NODE_USERS).child(userId).child(NODE_CONTACTS)

    fun ensureDefaultContacts(userId: String) {
        contactsRef(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) return

                val defaults = listOf(
                    Contact(
                        fullName = "Ana Méndez",
                        relationship = "Cónyuge",
                        phone = "987654321",
                        email = "ana.mendez@example.com"
                    ),
                    Contact(
                        fullName = "Carlos Ruiz",
                        relationship = "Hermano",
                        phone = "912345678",
                        email = "carlos.ruiz@example.com"
                    )
                )

                defaults.forEach { contact ->
                    val key = contactsRef(userId).push().key ?: return@forEach
                    contactsRef(userId).child(key).setValue(contact.withId(key))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignorar: solo es una inicialización opcional
            }
        })
    }

    fun addContact(userId: String, contact: Contact, onComplete: (Boolean) -> Unit) {
        val key = contact.id.ifBlank { contactsRef(userId).push().key }
        if (key.isNullOrBlank()) {
            onComplete(false)
            return
        }

        contactsRef(userId).child(key).setValue(contact.withId(key)) { error, _ ->
            onComplete(error == null)
        }
    }

    fun listenToContacts(
        userId: String,
        onResult: (List<Contact>) -> Unit,
        onError: (DatabaseError) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = snapshot.children.mapNotNull { child ->
                    child.getValue(Contact::class.java)?.withId(child.key.orEmpty())
                }
                onResult(contacts)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        }

        contactsRef(userId).addValueEventListener(listener)
        return listener
    }

    fun removeContactsListener(userId: String, listener: ValueEventListener) {
        contactsRef(userId).removeEventListener(listener)
    }
}
