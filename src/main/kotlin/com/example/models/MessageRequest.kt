package com.example.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MessageRequest(val type: MessageType, val value: String) {
    override fun toString(): String {
        return Json.encodeToString(this)
    }
}
