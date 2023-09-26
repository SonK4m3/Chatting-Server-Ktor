package com.example.models.tables

import org.jetbrains.exposed.sql.Table

object UserTable: Table("user") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255)
    val hashPassword = varchar("hashPassword", 512)
    val username = varchar("username", 512)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}