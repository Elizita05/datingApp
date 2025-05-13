package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class FotoPerfil(
    val idUsuario: Int,
    val rutaFoto: String,
    val esPrincipal: Boolean
)
