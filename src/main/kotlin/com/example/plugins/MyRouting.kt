package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configRouting() {
    routing {
        route("/Home", HttpMethod.Get) {
            handle {
                call.respondText("Home")
            }
        }

        route("/auth") {
            post("/register") {

            }

            post("/login") {

            }
        }

        authenticate("auth-jwt") {
            route ("/chats") {
                get ("/messages") {

                }

                webSocket("/message/{id}") {

                }
            }
            route ("/users") {
                route ("/friends") {
                    get {}

                    get ("/{id?}") {}

                    delete("/{id?}") {}
                }

                route ("/suggestions") {
                    get {}

                    get ("/{id?}") { }

                    post ("/add/{id?}") {}
                }
            }
        }
    }
}