package com.example.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ContactTable: Table("contacts") {
    val id = integer("id").autoIncrement()
    val profilePhoto = text("profilePhoto")
    val userId = reference("userId", UserTable.id, onDelete = ReferenceOption.SET_NULL)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}