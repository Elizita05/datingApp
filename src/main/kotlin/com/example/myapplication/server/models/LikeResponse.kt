package com.example.myapplication.server.models

import kotlinx.serialization.Serializable

@Serializable
data class LikeResponse(
    val match: Boolean,
    val message: String
)
