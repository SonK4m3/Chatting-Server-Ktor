package com.example.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object GroupTable: Table("groups") {
    val contactId = reference("contactId", ContactTable.id, onDelete = ReferenceOption.SET_NULL)
    val conversationId = reference("conversationId", ConversationTable.conversationId, onDelete = ReferenceOption.SET_NULL)
    val joinDate = long("joinDate")
    val leftDate = long("leftDate").nullable()
}