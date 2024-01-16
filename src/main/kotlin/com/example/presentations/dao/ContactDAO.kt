package com.example.presentations.dao

import com.example.models.Contact
import com.example.models.Friend
import com.example.models.User
import com.example.models.tables.ContactTable
import com.example.models.tables.FriendTable
import com.example.models.tables.UserTable
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.random.Random

class ContactDAO : DAO<Contact> {
    override fun resultRowToClass(row: ResultRow): Contact {
        return Contact(
            id = row[ContactTable.id],
            user = User(
                id = row[ContactTable.userId]
            ),
            profilePhoto = row[ContactTable.profilePhoto]
        )
    }

    override suspend fun readAll(): List<Contact> {
        return dbQuery {
            ContactTable
                .selectAll()
                .map(::resultRowToClass)
        }
    }

    override suspend fun read(id: Int): Contact? {
        return dbQuery {
            ContactTable
                .select { ContactTable.id eq id }
                .map(::resultRowToClass)
                .singleOrNull()
        }
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: Int, new: Contact): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun create(new: Contact): Contact? {
        return dbQuery {
            ContactTable
                .insert {
                    it[ContactTable.userId] = new.user.id
                    it[ContactTable.profilePhoto] = new.profilePhoto
                }.resultedValues?.map(::resultRowToClass)?.singleOrNull()
        }
    }

    suspend fun getRandomContactsSubset(userId: Int, limit: Int): List<Contact> {
        return dbQuery {
            val totalCount = ContactTable.selectAll().count()

            if (totalCount > 0) {
                val randomOffset = (0 until limit).map {
                    Random.nextLong(totalCount)
                }
                val randomRows = (ContactTable innerJoin UserTable)
                    .select { ContactTable.userId neq userId }
                    .limit(n = limit, offset = randomOffset.minOrNull() ?: 0)
                    .toList()

                randomRows.map { row ->
                    Contact(
                        id = row[ContactTable.id],
                        user = User(
                            id = row[ContactTable.userId],
                            username = row[UserTable.username],
                            email = "",
                            hashPassword = ""
                        ),
                        profilePhoto = row[ContactTable.profilePhoto]

                    )
                }
            } else {
                emptyList<Contact>()
            }
        }
    }

    suspend fun getContactByUserId(userId: Int): Contact? {
        return dbQuery {
            (ContactTable innerJoin UserTable)
                .select { ContactTable.userId eq userId }
                .map { row ->
                    Contact(
                        id = row[ContactTable.id],
                        user = User(
                            id = row[ContactTable.userId],
                            username = row[UserTable.username],
                            hashPassword = "",
                            email = ""
                        ),
                        profilePhoto = row[ContactTable.profilePhoto]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun getContactFriendList(contact: Contact): List<Contact> {
        return dbQuery {

            val friends = mutableListOf<Contact>()

            val friendList = Join(
                FriendTable, ContactTable,
                onColumn = FriendTable.friendId, otherColumn = ContactTable.id,
                joinType = JoinType.INNER,
                additionalConstraint = ({ FriendTable.contactId eq contact.id })
            )

            (friendList innerJoin UserTable).select {
                ContactTable.userId eq UserTable.id
            }.mapTo(friends) { row ->
                Contact(
                    id = row[FriendTable.friendId],
                    user = User(
                        id = row[ContactTable.userId], username = row[UserTable.username],
                        email = "",
                        hashPassword = ""
                    ),
                    profilePhoto = row[ContactTable.profilePhoto]
                )
            }

            val contactList = Join(
                FriendTable, ContactTable,
                onColumn = FriendTable.contactId, otherColumn = ContactTable.id,
                joinType = JoinType.INNER,
                additionalConstraint = ({ FriendTable.friendId eq contact.id })
            )

            (contactList innerJoin UserTable).select {
                ContactTable.userId eq UserTable.id
            }.mapTo(friends) { row ->
                Contact(
                    id = row[FriendTable.contactId],
                    user = User(
                        id = row[ContactTable.userId], username = row[UserTable.username],
                        email = "",
                        hashPassword = ""
                    ),
                    profilePhoto = row[ContactTable.profilePhoto]
                )
            }

            friends
        }
    }

}