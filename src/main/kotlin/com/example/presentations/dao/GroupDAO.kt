package com.example.presentations.dao

import com.example.models.Contact
import com.example.models.Conversation
import com.example.models.Group
import com.example.models.User
import com.example.models.tables.GroupTable
import com.example.models.tables.MessageTable
import org.jetbrains.exposed.sql.ResultRow
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class GroupDAO: DAO<Group> {
    override fun resultRowToClass(row: ResultRow): Group {
        return Group(
            contact = Contact(row[GroupTable.conversationId]),
            conversation = Conversation(row[GroupTable.conversationId]),
            joinDate = row[GroupTable.joinDate],
            leftDate = row[GroupTable.leftDate]!!
        )
    }

    override suspend fun readAll(): List<Group> {
        return dbQuery {
            GroupTable
                .selectAll()
                .map(::resultRowToClass)
        }
    }

    override suspend fun read(id: Int): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: Int, new: Group): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun create(new: Group): Group? {
        return dbQuery {
            GroupTable.insert {
                it[GroupTable.contactId] = new.contact.id
                it[GroupTable.conversationId] = new.conversation.id
                it[GroupTable.joinDate] = new.joinDate
                it[GroupTable.leftDate] = new.leftDate
            }.resultedValues?.map(::resultRowToClass)?.singleOrNull()
        }
    }
}