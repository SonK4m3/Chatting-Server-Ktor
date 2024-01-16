package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int,
    val user: User,
    val profilePhoto: String)
{
    constructor(id: Int) : this(id, User(-1), "")
}
