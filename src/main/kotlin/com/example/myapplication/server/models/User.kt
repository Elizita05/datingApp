package com.example.myapplication.server.models

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: Int,
    val email: String,
    val nombreUsuario: String,
    val fechaNacimiento: LocalDate,
    val genero: String,
    val orientacionSexual: String
)
