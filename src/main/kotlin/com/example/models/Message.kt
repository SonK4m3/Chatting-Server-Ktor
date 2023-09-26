package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val fromId: Int,
    val toId: Int,
    val sendDT: Long,
    val read: Boolean,
    val readDT: Long,
    val message: String
)
