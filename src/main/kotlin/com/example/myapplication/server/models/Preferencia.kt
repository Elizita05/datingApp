package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Preferencia(
    val idUsuario: Int,
    val rangoEdadMin: Int,
    val rangoEdadMax: Int,
    val generoDeseado: String,
    val distanciaMaxima: Int
)
