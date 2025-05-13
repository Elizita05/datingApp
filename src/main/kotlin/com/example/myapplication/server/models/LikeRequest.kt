package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class LikeRequest(
    val idUsuarioEmisor: Int,
    val idUsuarioReceptor: Int
)
