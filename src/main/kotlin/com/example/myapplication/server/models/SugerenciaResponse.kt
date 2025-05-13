package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class SugerenciaResponse(
    val idUsuario: Int,
    val nombre: String,
    val edad: Int,
    val genero: String,
    val fotoPerfil: String? // puede ser null si no tiene
)
