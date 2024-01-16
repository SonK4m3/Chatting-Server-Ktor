package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.*
import com.example.presentations.auth.hash
import com.example.presentations.dao.ConversationDAO
import com.example.presentations.dao.GroupDAO
import com.example.presentations.dao.MessageDAO
import com.example.presentations.dao.UserDAO
import com.example.presentations.dto.UserDTO
import com.example.presentations.routes.authRoute
import com.example.presentations.routes.messageRoute
import com.example.presentations.routes.webRoute
import com.example.presentations.service.ChattingService
import com.example.utils.dateTimeToLong
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

fun Application.configureRouting() {
    val secret = environment.config.property("jwt.secret").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val issuer = environment.config.property("jwt.issuer").getString()

    val hashFunction = { s: String -> hash(s) }
    val generateJWT = { user: User ->
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", user.id)
            .withClaim("email", user.email)
            .withExpiresAt(Date(System.currentTimeMillis() + 1200000))
            .sign(Algorithm.HMAC256(secret))
    }

    val userDAO = UserDAO()
    val chattingService = ChattingService()

    routing {
        authRoute(userDAO, generateJWT, hashFunction)

        route("hello") {
            get("/{name}") {
                val name = call.parameters["name"].toString()
                call.respond(HttpStatusCode.OK, "Hello $name")
            }
        }

        authenticate("auth-jwt") {
            get("/auth-hello") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respond(HttpStatusCode.OK, "Hello, $email! Token is expired at $expiresAt ms.")
            }

            route("/messages") {
                route("/personal") {
                    get("list-messages/{contactId}") {
                        val principal = call.principal<JWTPrincipal>()
                        val id = principal!!.payload.getClaim("id").asInt()
                        val receiverId = call.parameters["contactId"]!!.toInt()
                        val messages = chattingService.findPersonalConversationMessages(
                            id,
                            receiverId
                        )

                        call.respond(HttpStatusCode.OK, messages!!)
                    }

                    get("/recent-messages-by-time") {
                        val principal = call.principal<JWTPrincipal>()
                        val id = principal!!.payload.getClaim("id").asInt()
                        val receiverId = call.request.queryParameters["contactId"]?.toInt()
                        val untilTime = call.request.queryParameters["untilTime"]?.toLong()

                        if (id == null || receiverId == null || untilTime == null) {
                            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing some params", null))
                        }

                        val conversation = chattingService.findPersonalConversation(id, receiverId!!)

                        if (conversation != null) {
                            call.respond(HttpStatusCode.OK, DataResponse(false, "Conversation is not existing!", null))
                        }

                        val messages = chattingService.findRecentMessagesBeforeTimestamp(
                            conversation!!,
                            untilTime!!,
                            5
                        )

                        call.respond(HttpStatusCode.OK, messages)
                    }

                    get("/recent-messages-by-id") {
                        val principal = call.principal<JWTPrincipal>()
                        val id = principal!!.payload.getClaim("id").asInt()
                        val receiverId = call.request.queryParameters["receiverId"]?.toInt()
                        val messageId = call.request.queryParameters["messageId"]?.toInt()

                        if (id == null || receiverId == null || messageId == null) {
                            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing some params", null))
                        }

                        try {
                            val conversation = chattingService.findPersonalConversation(id, receiverId!!)

                            if (conversation == null) {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(false, "Conversation is not existing!", null)
                                )
                            }

                            val messages = if (messageId!! > 0) chattingService.findMessagesAfterMessageId(
                                conversation!!,
                                messageId,
                                5
                            ) else chattingService.findMessagesNewest(conversation!!, 5)

                            call.respond(HttpStatusCode.OK, DataResponse(true, "Success", messages))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(HttpStatusCode.Conflict)
                        }
                    }

                    get("/conversation/{contactId}") {
                        val principal = call.principal<JWTPrincipal>()
                        val id = principal!!.payload.getClaim("id").asInt()
                        val receiverId = call.parameters["contactId"]!!.toInt()
                        println("$id $receiverId")
                        val conversation = chattingService.findPersonalConversation(
                            id,
                            receiverId
                        )

                        call.respond(HttpStatusCode.OK, conversation!!)
                    }

                    post("/create") {
                        val conversation = Conversation(name = "Hotly night")
                        chattingService.createConversation(conversation)
                        call.respond(HttpStatusCode.Created, conversation)
                    }
                }

                route("/group") {
                    get("/{id}") {}
                    post("/{id}") {}
                    post("/create") {
                        val userId = call.request.queryParameters["userId"]
                        val conversationId = call.request.queryParameters["conversationId"]
                    }
                }
            }

            route("/users") {
                get {
                    val users: List<UserDTO> = chattingService.getUsers().map(UserDTO::convertToUserDTO);
                    call.respond(HttpStatusCode.OK, DataResponse(true, "success", users))
                }

                route("/friend") {
                    get("/list-friends") {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal!!.payload.getClaim("id").asInt()
                        try {
                            val sendContact = chattingService.getUserContact(userId)

                            if (sendContact == null)
                                call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Failure", null))

                            val friendList = chattingService.getFriendList(sendContact!!)
                            call.respond(HttpStatusCode.OK, DataResponse(true, "Success", friendList))

                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.Conflict)
                        }
                    }

                    get("/list-contacts") {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal!!.payload.getClaim("id").asInt()
                        try {
                            val sendContact = chattingService.getUserContact(userId)

                            if (sendContact == null)
                                call.respond(HttpStatusCode.OK, DataResponse(false, "Failure", null))

                            val friendList = chattingService.getContactFriendList(sendContact!!)
                            call.respond(HttpStatusCode.OK, DataResponse(true, "Success", friendList))

                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.Conflict)
                        }
                    }

                    get("/{id?}") {}

                    delete("/{id?}") {}
                }
            }

            route("contact") {
                get("/user") {
                    val userId = call.request.queryParameters["userId"]?.toInt()

                    if (userId == null)
                        call.respond(
                            HttpStatusCode.BadRequest,
                            DataResponse(false, "Missing some parameters", null)
                        )

                    val userContact: Contact? = chattingService.getUserContact(userId!!)
                    if (userContact == null) {
                        call.respond(
                            HttpStatusCode.OK,
                            DataResponse(false, "The user has not created contact information", null)
                        )
                    } else {
                        call.respond(HttpStatusCode.OK, DataResponse(true, "Success", userContact))
                    }
                }

                post("/add") {
//                    val userId = call.request.queryParameters["userId"]?.toInt()
                    val imageRequest = call.receive<ImageRequest>()
                    val userId = imageRequest.userId
                    val profilePhoto = imageRequest.profilePhoto
                    if (profilePhoto == "") {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            DataResponse(false, "Missing some parameters", null)
                        )
                    }
                    val initContact = Contact(-1, User(userId), profilePhoto)
                    try {
                        val newContact: Contact? = chattingService.createContact(initContact)

                        if (newContact != null) {
                            call.respond(
                                HttpStatusCode.Created,
                                DataResponse(true, "Success", true)
                            )
                        } else
                            call.respond(HttpStatusCode.Conflict, DataResponse(false, "Something wrong", null))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.Conflict, DataResponse(false, "Something wrong", null))
                    }
                }

                get("/suggestions") {
                    val id = call.request.queryParameters["id"]?.toInt()
                    try {
                        val contacts = chattingService.getRandomContactSubset(id!!, 5)

                        call.respond(HttpStatusCode.OK, DataResponse(true, "Success", contacts))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }

            route("/invitation") {
                get("/requests") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("id").asInt()
                    try {
                        val sendContact = chattingService.getUserContact(userId)

                        if (sendContact == null)
                            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Failure", null))

                        val invitationList = chattingService.invitationRequests(sendContact!!)
                        call.respond(HttpStatusCode.OK, DataResponse(true, "Success", invitationList))

                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.Conflict)
                    }
                }

                post("/send") {
                    val principal = call.principal<JWTPrincipal>()
                    val sendUserid = principal!!.payload.getClaim("id").asInt()
                    val receiverUserId = call.request.queryParameters["userId"]!!.toInt()
                    val time = dateTimeToLong()
                    try {
                        val sendContact = chattingService.getUserContact(sendUserid)

                        if (sendContact == null)
                            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Failure", null))

                        val invitation = Invitation(
                            id = -1,
                            contact = Contact(receiverUserId),
                            fromContact = sendContact!!,
                            sendTime = time,
                            response = false,
                            isAccept = false
                        )

                        if (chattingService.sendInvitation(invitation) != null) {
                            call.respond(HttpStatusCode.OK, DataResponse(true, "Success", true))
                        } else {
                            call.respond(HttpStatusCode.Conflict, DataResponse(false, "Something wrong", null))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.Conflict)
                    }
                }

                post("/response") {
                    val principal = call.principal<JWTPrincipal>()
                    val sendUserid = principal!!.payload.getClaim("id").asInt()
                    val invitationId = call.request.queryParameters["invitationId"]?.toInt()
                    val isAccept = call.request.queryParameters["isAccept"]?.toInt()
                    val contactId = call.request.queryParameters["contactId"]?.toInt()
                    val fromContactId = call.request.queryParameters["fromContactId"]?.toInt()

                    if (sendUserid == null || invitationId == null || isAccept == null || contactId == null || fromContactId == null)
                        call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing params", null))

                    try {
                        val invitation = Invitation(
                            id = invitationId!!,
                            contact = Contact(contactId!!),
                            fromContact = Contact(fromContactId!!),
                            sendTime = 0,
                            response = true,
                            isAccept = isAccept == 1
                        )
                        val success = chattingService.updateInvitation(invitation)

                        if (success) {
                            if (isAccept == 1) {
                                val time = dateTimeToLong()
                                val newFriend = Friend(
                                    -1,
                                    Contact(invitation.contact.id),
                                    Contact(invitation.fromContact.id),
                                    time
                                )
                                val createdFriendSuccess = chattingService.makeFriend(newFriend)
                                if (createdFriendSuccess != null) {
                                    call.respond(HttpStatusCode.OK, DataResponse(true, "Success", true))
                                } else {
                                    call.respond(HttpStatusCode.OK, DataResponse(false, "Something wrong", null))
                                }
                            }
                            call.respond(HttpStatusCode.OK, DataResponse(true, "Success", true))
                        } else {
                            call.respond(HttpStatusCode.OK, DataResponse(false, "Something wrong", null))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.Conflict)
                    }
                }
            }
        }

        messageRoute(chattingService)

        route("/hello") {
            get {
                call.respond("{\"message\":\"Hello world!\"}")
            }
        }

        webRoute()
    }
}
