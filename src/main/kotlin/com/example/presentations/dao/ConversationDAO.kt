package com.example.presentations.dao;

import com.example.models.Contact
import com.example.models.Conversation
import com.example.models.tables.ConversationTable
import com.example.models.tables.GroupTable
import com.example.models.tables.UserTable
import com.example.presentations.repositories.DatabaseFactory
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

public class ConversationDAO : DAO<Conversation> {
    override fun resultRowToClass(row: ResultRow): Conversation {
        return Conversation(
            id = row[ConversationTable.conversationId],
            name = row[ConversationTable.conversationName]
        )
    }

    override suspend fun readAll(): List<Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun read(id: Int): Conversation? {
        return dbQuery {
            ConversationTable
                .select { ConversationTable.conversationId eq id }
                .singleOrNull()
                ?.let {
                    Conversation(
                        id = it[ConversationTable.conversationId],
                        name = it[ConversationTable.conversationName]
                    )
                }
        }
    }

    override suspend fun delete(id: Int): Boolean {
        return dbQuery {
            val deletedRows = ConversationTable
                .deleteWhere { ConversationTable.conversationId eq id }
            deletedRows > 0
        }
    }

    override suspend fun update(id: Int, new: Conversation): Boolean {
        return dbQuery {
            val updatedRows = ConversationTable
                .update({ ConversationTable.conversationId eq id }) {
                    it[ConversationTable.conversationName] = new.name
                }
            updatedRows > 0
        }
    }

    override suspend fun create(new: Conversation): Conversation? {
        return dbQuery {
            ConversationTable.insert {
                it[ConversationTable.conversationName] = new.name
            }.resultedValues?.map(::resultRowToClass)?.singleOrNull()
        }
    }

    suspend fun findPersonalConversation(contact1: Contact, contact2: Contact): Conversation? {
        return dbQuery {
            ConversationTable
                .slice(ConversationTable.columns)
                .select {
                    ConversationTable.conversationId inList GroupTable
                        .innerJoin(
                            GroupTable.alias("g2"),
                            { conversationId },
                            { GroupTable.alias("g2")[GroupTable.conversationId] })
                        .slice(GroupTable.conversationId)
                        .select { (GroupTable.contactId eq contact1.id) and (GroupTable.alias("g2")[GroupTable.contactId] eq contact2.id) }
                        .map { it[GroupTable.conversationId] }
                }
                .map {
                    Conversation(
                        id = it[ConversationTable.conversationId],
                        name = it[ConversationTable.conversationName]
                    )
                }.singleOrNull()
        }
    }
}
