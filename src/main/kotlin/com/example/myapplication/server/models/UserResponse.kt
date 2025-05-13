package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val nombreUsuario: String,
    val fechaNacimiento: String,
    val genero: String,
    val orientacionSexual: String
)