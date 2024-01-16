package com.example.models

import kotlinx.serialization.Serializable

@Serializable
enum class MessageType(val nameType: String) {
    TEXT("text"),
    IMAGE("image"),
    RECORD("record"),
}