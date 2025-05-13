package com.example.myapplication.server.models

@kotlinx.serialization.Serializable
data class RechazoRequest(
    val idUsuarioEmisor: Int,
    val idUsuarioReceptor: Int
)

