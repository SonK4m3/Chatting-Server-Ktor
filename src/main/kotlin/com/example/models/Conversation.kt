package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    var id: Int = -1,
    val name: String
) {
    constructor(id: Int) : this(id, "")
}
