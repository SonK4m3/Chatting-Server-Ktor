package com.example.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object FriendTable: Table("friends") {
    val id = integer("id").autoIncrement()
    val contactId = reference("contactId", ContactTable.id, onDelete = ReferenceOption.CASCADE)
    val friendId = integer("friendId").references(ContactTable.id)
    val addTime = long("addTime")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}