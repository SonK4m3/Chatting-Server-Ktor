package com.example.presentations.dao

import com.example.models.Contact
import com.example.models.Friend
import com.example.models.User
import com.example.models.tables.ContactTable
import com.example.models.tables.FriendTable
import com.example.models.tables.UserTable
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class FriendDAO : DAO<Friend> {
    override fun resultRowToClass(row: ResultRow): Friend {
        return Friend(
            id = row[FriendTable.id],
            contact = Contact(row[FriendTable.contactId]),
            friend = Contact(row[FriendTable.friendId]),
            addTime = row[FriendTable.addTime]
        )
    }

    override suspend fun readAll(): List<Friend> {
        TODO("Not yet implemented")
    }

    override suspend fun read(id: Int): Friend? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: Int, new: Friend): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun create(new: Friend): Friend? {
        return dbQuery {
            FriendTable
                .insert {
                    it[FriendTable.contactId] = new.contact.id
                    it[FriendTable.friendId] = new.friend.id
                    it[FriendTable.addTime] = new.addTime
                }.resultedValues?.map(::resultRowToClass)?.singleOrNull()
        }
    }

    suspend fun getFriendList(contact: Contact): List<Friend> {
        return dbQuery {
            val friendList = Join(
                FriendTable, ContactTable,
                onColumn = FriendTable.friendId, otherColumn = ContactTable.id,
                joinType = JoinType.INNER,
                additionalConstraint = ({FriendTable.contactId eq contact.id})
            )

            (friendList innerJoin UserTable).select{
                ContactTable.userId eq UserTable.id
            }.map {
                row -> Friend(
                    id = row[FriendTable.id],
                    contact = Contact(row[FriendTable.contactId]),
                    friend = Contact(
                        id = row[FriendTable.friendId],
                        user = User(
                            id = row[ContactTable.userId], username = row[UserTable.username],
                            email = "",
                            hashPassword = ""
                        ),
                        profilePhoto = row[ContactTable.profilePhoto]
                    ),
                    addTime = row[FriendTable.addTime]
                )
            }
        }
    }
}