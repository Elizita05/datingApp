package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Like(
    val idUsuarioEmisor: Int,
    val idUsuarioReceptor: Int,
    val estado: String = "like" // "like" o "rechazado"
)