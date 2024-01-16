package com.example.presentations.service

import com.example.models.*
import com.example.models.tables.ConversationTable
import com.example.models.tables.GroupTable
import com.example.presentations.dao.*
import com.example.presentations.repositories.DatabaseFactory
import com.example.utils.dateTimeToLong
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select

class ChattingService {
    private var userDao: UserDAO = UserDAO()
    private var groupDao: GroupDAO = GroupDAO()
    private var messageDao: MessageDAO = MessageDAO()
    private var conversationDao: ConversationDAO = ConversationDAO()
    private var contactDao: ContactDAO = ContactDAO()
    private var invitationDao: InvitationDAO = InvitationDAO()
    private var friendDao: FriendDAO = FriendDAO()
    fun getUsers(): List<User> {
        return runBlocking {
            userDao.readAll()
        }
    }

    fun findUserById(id: Int): User? {
        return runBlocking {
            userDao.read(id)
        }
    }

    fun getRandomContactSubset(id: Int, limit: Int): List<Contact> {
        return runBlocking {
            contactDao.getRandomContactsSubset(id, limit)
        }
    }

    fun createContact(contact: Contact): Contact? {
        return runBlocking {
            contactDao.create(contact)
        }
    }

    fun getUserContact(userId: Int): Contact? {
        return runBlocking {
            contactDao.getContactByUserId(userId)
        }
    }

    fun findPersonalConversation(userId1: Int, userId2: Int): Conversation? {
        return runBlocking {
            val contact1 = getUserContact(userId1)
            val contact2 = getUserContact(userId2)
            if (contact1 == null || contact2 == null) return@runBlocking null
            conversationDao.findPersonalConversation(contact1, contact2)
        }
    }

    fun findPersonalConversationMessages(userId1: Int, userId2: Int): List<Message>? {
        var messages: List<Message>? = null
        runBlocking {
            val contact1 = getUserContact(userId1)
            val contact2 = getUserContact(userId2)
            val conversation = conversationDao.findPersonalConversation(contact1!!, contact2!!) ?: return@runBlocking
            messages = conversation.let { messageDao.findMessageInConversation(it) }
        }
        return messages;
    }

    fun createConversation(conversation: Conversation): Boolean =
        try {
            runBlocking {
                conversation.id = conversationDao.create(conversation)!!.id
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    fun createPersonalConversation(conversation: Conversation, user1: Contact, user2: Contact) {
        val createDT: Long = dateTimeToLong()
        try {
            runBlocking {
                conversation.id = conversationDao.create(conversation)!!.id
                groupDao.create(Group(user1, conversation, createDT, -1L))
                groupDao.create(Group(user2, conversation, createDT, -1L))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createMessage(message: Message): Message? {
        return runBlocking {
            messageDao.create(message)
        }
    }

    fun invitationRequests(contact: Contact): List<Invitation> {
        return runBlocking {
            invitationDao.getInvitationRequests(contact)
        }
    }

    fun sendInvitation(invitation: Invitation): Invitation? {
        return runBlocking {
            val isExist = invitationDao.getInvitationContact(invitation)
            println(invitation.id)
            if (isExist) {
                invitationDao.updateResponse(invitation)
                invitation
            } else {
                invitationDao.create(invitation)
            }
        }
    }

    fun updateInvitation(invitation: Invitation): Boolean {
        return runBlocking {
            invitationDao.updateResponse(invitation)
        }
    }

    fun makeFriend(friend: Friend): Friend? {
        return runBlocking {
            friendDao.create(friend)
        }
    }

    fun getFriendList(contact: Contact): List<Friend> {
        return runBlocking {
            friendDao.getFriendList(contact)
        }
    }

    fun getContactFriendList(contact: Contact): List<Contact> {
        return runBlocking {
            contactDao.getContactFriendList(contact)
        }
    }

    fun findRecentMessagesBeforeTimestamp(conversation: Conversation, timestamp: Long, limit: Int): List<Message> {
        return runBlocking {
            messageDao.findRecentMessagesBeforeTimestamp(conversation, timestamp, limit)
        }
    }

    fun findMessagesAfterMessageId(conversation: Conversation, id: Int, limit: Int): List<Message> {
        return runBlocking {
            messageDao.findMessagesAfterMessageId(conversation, id, limit)
        }
    }

    fun findMessagesNewest(conversation: Conversation, limit: Int): List<Message> {
        return runBlocking {
            messageDao.findMessagesNewest(conversation, limit)
        }
    }
}