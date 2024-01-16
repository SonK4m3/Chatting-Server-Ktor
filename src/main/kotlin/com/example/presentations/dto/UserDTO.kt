package com.example.presentations.dto

import com.example.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(val id: Int, val username: String, val email: String) {
    companion object {
        fun convertToUserDTO(u: User): UserDTO = UserDTO(u.id, u.username, u.email)
    }
}
