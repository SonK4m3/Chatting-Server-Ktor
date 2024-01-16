package com.example.presentations.routes

import com.example.models.*
import com.example.presentations.auth.verifyJWTToken
import com.example.presentations.dto.MessageDTO
import com.example.presentations.service.ChattingService
import com.example.utils.dateTimeToLong
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun Route.messageRoute(
    chattingService: ChattingService
) {
    val session = Collections.synchronizedSet<Connection?>(LinkedHashSet()) // luu thong tin nguoi ket noi

    webSocket("/message") {
        val principal = call.principal<JWTPrincipal>()
        val email = principal!!.payload.getClaim("email").asString()    // lay email nguoi ket noi

        val userConnection = Connection(this, email)
        session += userConnection
        // nhan tin nhan va gui cho nguoi khac
        try {
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val message = frame.readText()
                val textUser = "[user $email : $message]"
                session.forEach { if (it != userConnection) it.session.send(textUser) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            send("Exit room chat")
            session -= userConnection
        }
    }

    webSocket("/chat/personal/{receiverId}") {
        val connections = mutableListOf<Connection>()
        val receiverId = call.parameters["receiverId"]
        val principal = call.authentication.principal<JWTPrincipal>()
        val senderId = principal!!.payload.getClaim("id")

        if (receiverId == null || senderId == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid parameters"))
            return@webSocket
        }

        var conversation = chattingService.findPersonalConversation(senderId.asInt(), receiverId.toInt())

        if (conversation == null) {
            conversation = Conversation(name = "personal chat $senderId $receiverId")
            chattingService.createPersonalConversation(
                conversation,
                Contact(senderId.asInt()),
                Contact(receiverId.toInt())
            )
        }

        val userConnection = Connection(this, senderId.asInt().toString())
        session += userConnection
        try {
            send("Connected to chat with $receiverId")
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val sendTime = dateTimeToLong()
                    val message =
                        Message(conversation, senderId.asInt(), sendTime, false, -1, MessageType.TEXT, frame.readText())
                    chattingService.createMessage(message)
                    session.forEach { it ->
                        if (it.email == receiverId) {
                            it.session.send(Json.encodeToString(MessageDTO.convertToMessageDTO(message)))

                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle connection errors or user leaving the chat
        } finally {
            session -= userConnection
        }
    }

    webSocket("/chat/personal/public/{receiverId}") {
        val connections = mutableListOf<Connection>()
        val receiverId = call.parameters["receiverId"]
        val token = call.request.queryParameters["token"]
        var senderId: String? = null

        if (token != null) {
            val jwtPayload = verifyJWTToken(token)?.claims
            if (jwtPayload != null) {
                senderId = jwtPayload["id"].toString()
            } else {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid payload"))
                return@webSocket
            }
        } else {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing token"))
            return@webSocket
        }

        if (receiverId == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid parameters"))
            return@webSocket
        }

        println("receiver: $receiverId , senderID: $senderId")

        var conversation = chattingService.findPersonalConversation(senderId.toInt(), receiverId.toInt())

        if (conversation == null) {
            conversation = Conversation(name = "personal chat $senderId $receiverId")
            val contact1 = chattingService.getUserContact(senderId.toInt())
            val contact2 = chattingService.getUserContact(receiverId.toInt())
            if (contact1 == null || contact2 == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid parameters"))
                return@webSocket
            }
            chattingService.createPersonalConversation(conversation!!, contact1, contact2)
        }

        val userConnection = Connection(this, senderId)
        session += userConnection
        try {
            send(Json.encodeToString(DataResponse(true, "success", "$senderId connected to chat with $receiverId")))
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val sendTime = dateTimeToLong()
                    val mess = Json.decodeFromString(MessageRequest.serializer(), frame.readText())
                    var message = Message(
                        conversation!!,
                        senderId.toInt(),
                        sendTime,
                        false,
                        -1,
                        mess.type,
                        mess.value
                    )
                    message = chattingService.createMessage(message)!!
                    session.forEach { it ->
                        if (it.email == receiverId || it.email == senderId) {
                            it.session.send(Json.encodeToString(MessageDTO.convertToMessageDTO(message)))

                        }
                    }
                } else if (frame is Frame.Binary) {
                    val sendTime = dateTimeToLong()
                    val directoryPath = "D:\\Documents\\video"
                    val fileName = "${sendTime}.mp4"
                    val fileBytes = frame.readBytes()

                    val directory = File(directoryPath)
                    if (!directory.exists()) directory.mkdirs()

                    File(directory, fileName).writeBytes(fileBytes)

                    var message = Message(
                        conversation!!,
                        senderId.toInt(),
                        sendTime,
                        false,
                        -1,
                        MessageType.RECORD,
                        fileName
                    )
                    message = chattingService.createMessage(message)!!
                    session.forEach { it ->
                        if (it.email == receiverId || it.email == senderId) {
                            it.session.send(
                                Json.encodeToString(
                                    MessageDTO.convertToMessageDTO(
                                        Message(
                                            conversation!!,
                                            senderId.toInt(),
                                            sendTime,
                                            false,
                                            -1,
                                            MessageType.RECORD,
                                            null
                                        )
                                    )
                                )
                            )
                            it.session.send(Frame.Binary(true, fileBytes))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle connection errors or user leaving the chat
        } finally {
            session -= userConnection
        }
    }
}