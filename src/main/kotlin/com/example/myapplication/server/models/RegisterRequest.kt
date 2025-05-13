package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable

data class RegisterRequest(
    val email: String,
    val password: String,
    val nombreUsuario: String,
    val fechaNacimiento: String, // formato: yyyy-MM-dd
    val genero: String,
    val orientacionSexual: String
)

