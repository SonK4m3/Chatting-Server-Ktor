package com.example.presentations.dao

import com.example.models.Contact
import com.example.models.Invitation
import com.example.models.User
import com.example.models.tables.ContactTable
import com.example.models.tables.InvitationTable
import com.example.models.tables.UserTable
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class InvitationDAO : DAO<Invitation> {
    override fun resultRowToClass(row: ResultRow): Invitation {
        return Invitation(
            id = row[InvitationTable.id],
            contact = Contact(row[InvitationTable.contactId]),
            fromContact = Contact(row[InvitationTable.fromContactId]),
            sendTime = row[InvitationTable.sendTime],
            response = row[InvitationTable.response],
            isAccept = row[InvitationTable.isAccept]
        )
    }

    override suspend fun readAll(): List<Invitation> {
        TODO("Not yet implemented")
    }

    override suspend fun read(id: Int): Invitation? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun update(id: Int, new: Invitation): Boolean {
        return dbQuery {
            val updateRows = InvitationTable.update({ InvitationTable.id eq id }) {
                it[InvitationTable.contactId] = new.contact.id
                it[InvitationTable.fromContactId] = new.fromContact.id
                it[InvitationTable.sendTime] = new.sendTime
                it[InvitationTable.response] = new.response
                it[InvitationTable.isAccept] = new.isAccept
            }
            updateRows > 0
        }
    }

    override suspend fun create(new: Invitation): Invitation? {
        return dbQuery {
            InvitationTable
                .insert {
                    it[InvitationTable.contactId] = new.contact.id
                    it[InvitationTable.fromContactId] = new.fromContact.id
                    it[InvitationTable.sendTime] = new.sendTime
                    it[InvitationTable.response] = new.response
                    it[InvitationTable.isAccept] = new.isAccept
                }.resultedValues?.map(::resultRowToClass)?.singleOrNull()
        }
    }

    suspend fun getInvitationRequests(contact: Contact): List<Invitation> {
        return dbQuery {
            val invitationContactTbl = Join(
                InvitationTable, ContactTable,
                onColumn = InvitationTable.fromContactId, otherColumn = ContactTable.id,
                joinType = JoinType.INNER,
                additionalConstraint = {
                    (InvitationTable.contactId eq contact.id) and (InvitationTable.response eq false)
                }
            )

            (invitationContactTbl innerJoin UserTable).select {
                ContactTable.userId eq UserTable.id
            }.map { row ->
                Invitation(
                    id = row[InvitationTable.id],
                    contact = Contact(
                        id = row[InvitationTable.contactId]
                    ),

                    fromContact = Contact(
                        id = row[InvitationTable.fromContactId],
                        user = User(
                            id = row[ContactTable.userId], username = row[UserTable.username],
                            email = "",
                            hashPassword = ""
                        ),
                        profilePhoto = row[ContactTable.profilePhoto]
                    ), sendTime = row[InvitationTable.sendTime],
                    response = row[InvitationTable.response],
                    isAccept = row[InvitationTable.isAccept]
                )
            }
        }
    }

    suspend fun updateResponse(invitation: Invitation): Boolean {
        return dbQuery {
            val updateRows = InvitationTable.update({ InvitationTable.id eq invitation.id }) {
                it[InvitationTable.sendTime] = invitation.sendTime
                it[InvitationTable.response] = invitation.response
                it[InvitationTable.isAccept] = invitation.isAccept
            }
            updateRows > 0
        }
    }

    suspend fun getInvitationContact(invitation: Invitation): Boolean {
        return dbQuery {
            val record = InvitationTable
                .select {
                    (InvitationTable.contactId eq invitation.contact.id) and (InvitationTable.fromContactId eq invitation.fromContact.id) and (InvitationTable.isAccept eq false)
                }.map(::resultRowToClass).lastOrNull()

            record?.let {
                invitation.id = it.id
            }
            
            record != null
        }
    }
}