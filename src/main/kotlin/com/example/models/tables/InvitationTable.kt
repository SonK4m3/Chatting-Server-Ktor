package com.example.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object InvitationTable : Table("invitations") {
    val id = integer("id").autoIncrement()
    val contactId = reference("contactId", ContactTable.id, onDelete = ReferenceOption.CASCADE)
    val fromContactId = integer("fromContactId")
    val sendTime = long("sendTime")
    val response = bool("response")
    val isAccept = bool("isAccept")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}