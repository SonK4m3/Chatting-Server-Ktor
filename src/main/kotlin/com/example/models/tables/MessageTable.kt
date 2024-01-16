package com.example.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object MessageTable: Table("message") {
    val id = integer("id").autoIncrement()
    val conversationId = reference("conversationId", ConversationTable.conversationId, onDelete = ReferenceOption.CASCADE)
    val fromId = integer("fromId")
    val sendDT = long("sendDT")
    val read = bool("read")
    val readDT = long("readDT")
    val messageType = text("messageType")
    val message = text("message")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}