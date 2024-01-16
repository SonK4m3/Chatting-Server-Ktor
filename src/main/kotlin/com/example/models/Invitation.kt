package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Invitation(
    var id: Int,
    val contact: Contact,
    val fromContact: Contact,
    val sendTime: Long,
    val response: Boolean,
    val isAccept: Boolean
)
