package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable

data class Mensaje(
    val idMensaje: Int,
    val idRemitente: Int,
    val idDestinatario: Int,
    val contenido: String,
    val fechaEnvio: String
)