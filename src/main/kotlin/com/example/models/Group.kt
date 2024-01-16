package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val contact: Contact,
    val conversation: Conversation,
    val joinDate: Long,
    val leftDate: Long
)
