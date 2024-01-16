package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int = -1,
    val conversation: Conversation,
    val fromId: Int,
    val sendDT: Long,
    val read: Boolean,
    val readDT: Long,
    val messageType: MessageType,
    val message: String?
) {
    constructor(
        conversation: Conversation,
        fromId: Int,
        sendDT: Long,
        read: Boolean,
        readDT: Long,
        messageType: MessageType,
        message: String?
    ) : this(
        -1,
        conversation,
        fromId,
        sendDT,
        read,
        readDT,
        messageType,
        message
    )
}
