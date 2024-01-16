package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    @SerialName("password") val hashPassword: String,
    val username: String
) {
    constructor(id: Int): this(id, "", "", "")
}
