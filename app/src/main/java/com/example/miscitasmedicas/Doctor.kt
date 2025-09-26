package com.example.miscitasmedicas

data class Doctor(
    val name: String,
    val cmp: String,
    val specialtyId: String,
    val emoji: String = "👨‍⚕️"
)

fun defaultDoctors(): List<Doctor> = listOf(
    Doctor(name = "Dra. María Rojas", cmp = "CMP 102030", specialtyId = "cardio", emoji = "❤️"),
    Doctor(name = "Dr. Juan Pérez", cmp = "CMP 403020", specialtyId = "cardio"),
    Doctor(name = "Dra. Lucía Soto", cmp = "CMP 556677", specialtyId = "derma", emoji = "🧴"),
    Doctor(name = "Dr. Luis Campos", cmp = "CMP 998877", specialtyId = "pedia", emoji = "🧸"),
    Doctor(name = "Dra. Ana Rivera", cmp = "CMP 445566", specialtyId = "gine", emoji = "🌸"),
    Doctor(name = "Dr. Jorge Medina", cmp = "CMP 334455", specialtyId = "neuro", emoji = "🧠"),
    Doctor(name = "Dra. Sofía Lozano", cmp = "CMP 223344", specialtyId = "ofta", emoji = "👁️"),
    Doctor(name = "Dr. Carlos Gómez", cmp = "CMP 112233", specialtyId = "trauma", emoji = "🦴"),
    Doctor(name = "Lic. Paula Cruz", cmp = "CPP 778899", specialtyId = "psico", emoji = "🧘"),
)
