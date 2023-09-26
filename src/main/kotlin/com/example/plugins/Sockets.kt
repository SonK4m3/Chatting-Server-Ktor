package com.example.plugins

import com.example.models.Connection
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.Collections

fun Application.configureSockets() {
    install(WebSockets)

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        webSocket("/chat") {
            send("Adding user!") // websocketSession
            val userConnection = Connection(this)
            connections += userConnection

            try {
                send("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val message = frame.readText()
                    val textWithUser = "[${userConnection.name}]: $message"
                    connections.forEach { it.session.send(textWithUser) }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $userConnection")
                connections -= userConnection
            }
        }
    }
}
