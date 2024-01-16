package com.example.models.tables

import org.jetbrains.exposed.sql.Table

object ConversationTable: Table("conversations") {
    val conversationId = integer("conversationId").autoIncrement()
    val conversationName = varchar("conversationName", 255)

    override val primaryKey: PrimaryKey = PrimaryKey(conversationId)
}