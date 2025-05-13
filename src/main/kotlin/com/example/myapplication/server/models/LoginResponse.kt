package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class LoginResponse(
    val mensaje: String,
    val usuario: UserResponse
)
