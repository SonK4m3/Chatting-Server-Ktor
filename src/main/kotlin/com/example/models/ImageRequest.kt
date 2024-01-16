package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageRequest(val userId: Int, val profilePhoto: String)
