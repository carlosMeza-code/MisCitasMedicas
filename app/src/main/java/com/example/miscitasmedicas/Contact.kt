package com.example.miscitasmedicas

/**
 * Representa un contacto o persona de confianza que se puede compartir en la app.
 */
data class Contact(
    val id: String = "",
    val fullName: String = "",
    val relationship: String = "",
    val phone: String = "",
    val email: String = ""
) {
    fun withId(newId: String): Contact = copy(id = newId)
}
