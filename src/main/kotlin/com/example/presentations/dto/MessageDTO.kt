package com.example.presentations.dto

import com.example.models.Message
import com.example.models.MessageType
import com.example.models.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MessageDTO(
    val fromId: Int, val sendDT: Long,
    val read: Boolean,
    val readDT: Long,
    val messageType: MessageType,
    val message: String?
) {
    override fun toString(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun convertToMessageDTO(m: Message): MessageDTO =
            MessageDTO(m.fromId, m.sendDT, m.read, m.readDT, m.messageType, m.message)
    }
}