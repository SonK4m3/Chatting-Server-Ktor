package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class DataResponse<T>(
    val success: Boolean,
    val message: String,
    val result: T
)
