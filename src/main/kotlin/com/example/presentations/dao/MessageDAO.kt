package com.example.presentations.dao;

import com.example.models.Conversation
import com.example.models.Message
import com.example.models.MessageType
import com.example.models.tables.MessageTable
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class MessageDAO : DAO<Message> {
    override fun resultRowToClass(row: ResultRow): Message =
        Message(
            id = row[MessageTable.id],
            conversation = Conversation(row[MessageTable.conversationId]),
            fromId = row[MessageTable.fromId],
            sendDT = row[MessageTable.sendDT],
            read = row[MessageTable.read],
            readDT = row[MessageTable.readDT],
            messageType = when(row[MessageTable.messageType]) {
                "text" -> MessageType.TEXT
                "image" -> MessageType.IMAGE
                else -> MessageType.RECORD
            },
            message = row[MessageTable.message]
        )

    override suspend fun readAll(): List<Message> {
        return dbQuery {
            MessageTable
                .selectAll()
                .map(::resultRowToClass)
        }
    }

    override suspend fun read(id: Int): Message? {
        return dbQuery {
            MessageTable
                .select { MessageTable.id eq id }
                .map(::resultRowToClass)
                .singleOrNull()
        }
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: Int, new: Message): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun create(new: Message): Message? {
        return dbQuery {
            MessageTable
                .insert {
                    it[MessageTable.conversationId] = new.conversation.id
                    it[MessageTable.fromId] = new.fromId
                    it[MessageTable.sendDT] = new.sendDT
                    it[MessageTable.read] = new.read
                    it[MessageTable.readDT] = new.readDT
                    it[MessageTable.messageType] = new.messageType.nameType
                    it[MessageTable.message] = new.message ?: ""
                }.resultedValues?.map(::resultRowToClass)?.singleOrNull()
        }
    }

    suspend fun findMessageInConversation(conversation: Conversation): List<Message> {
        return dbQuery {
            MessageTable
                .select { MessageTable.conversationId eq conversation.id }
                .map {
                    Message(
                        id = it[MessageTable.id],
                        conversation = conversation,
                        fromId = it[MessageTable.fromId],
                        sendDT = it[MessageTable.sendDT],
                        read = it[MessageTable.read],
                        readDT = it[MessageTable.readDT],
                        messageType = when(it[MessageTable.messageType]) {
                            "text" -> MessageType.TEXT
                            "image" -> MessageType.IMAGE
                            else -> MessageType.RECORD
                        },
                        message = it[MessageTable.message]
                    )
                }
        }
    }

    suspend fun findRecentMessagesBeforeTimestamp(conversation: Conversation, timestamp: Long, limit: Int): List<Message> {
        return dbQuery {
            MessageTable
                .select {
                    (MessageTable.conversationId eq conversation.id) and
                            (MessageTable.sendDT lessEq timestamp)
                }
                .orderBy(MessageTable.sendDT to SortOrder.DESC)
                .limit(limit)
                .map(::resultRowToClass)
        }
    }

    suspend fun findMessagesAfterMessageId(conversation: Conversation, messageId: Int, limit: Int): List<Message> {
        return dbQuery {
            MessageTable
                .select {
                    (MessageTable.conversationId eq conversation.id) and
                            (MessageTable.id less messageId)
                }
                .orderBy(MessageTable.id to SortOrder.ASC) // Assuming newer messages have higher IDs
                .limit(limit)
                .map(::resultRowToClass)
        }
    }

    suspend fun findMessagesNewest(conversation: Conversation, limit: Int): List<Message> {
        return dbQuery {
            MessageTable
                .select {
                    (MessageTable.conversationId eq conversation.id)
                }
                .orderBy(MessageTable.id to SortOrder.DESC)
                .limit(limit)
                .map(::resultRowToClass).reversed()
        }
    }
}
