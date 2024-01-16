package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    var id: Int,
    val contact: Contact,
    val friend: Contact,
    val addTime: Long
)
