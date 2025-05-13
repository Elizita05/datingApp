package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable

data class Verify2FARequest(
    val email: String,
    val codigo: String
)

