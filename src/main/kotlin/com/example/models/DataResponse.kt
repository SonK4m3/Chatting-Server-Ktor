package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class DataResponse(
    val success: Boolean,
    val message: String
)
