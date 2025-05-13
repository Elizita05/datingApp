package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val userId: Int,
    val bio: String? = null,
    val location: String? = null,
    val interests: String? = null,
    val favoriteMusic: String? = null,
    val favoriteMovies: String? = null
)
