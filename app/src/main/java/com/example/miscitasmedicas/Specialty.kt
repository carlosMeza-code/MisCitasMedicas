package com.example.miscitasmedicas

data class Specialty(
    val id: String,
    val name: String,
    val emoji: String
)

fun defaultSpecialties(): List<Specialty> = listOf(
    Specialty(id = "cardio", name = "Cardiología", emoji = "❤️"),
    Specialty(id = "derma", name = "Dermatología", emoji = "🧴"),
    Specialty(id = "pedia", name = "Pediatría", emoji = "🧸"),
    Specialty(id = "gine", name = "Ginecología", emoji = "🌸"),
    Specialty(id = "neuro", name = "Neurología", emoji = "🧠"),
    Specialty(id = "ofta", name = "Oftalmología", emoji = "👁️"),
    Specialty(id = "trauma", name = "Traumatología", emoji = "🦴"),
    Specialty(id = "psico", name = "Psicología", emoji = "🧘"),
)
