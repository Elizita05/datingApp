package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class MensajeRequest(
    val idRemitente: Int,
    val idDestinatario: Int,
    val contenido: String
)