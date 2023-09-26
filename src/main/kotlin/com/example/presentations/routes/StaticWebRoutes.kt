package com.example.presentations.routes

import com.example.models.articles
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.webRoute() {
    staticResources("/static","files") {

    }

    get ("/") {
        call.respondRedirect("articles")
    }
    route("articles") {
        get {
            call.respond(FreeMarkerContent("index.ftl", mapOf("articles" to articles)))
        }
    }
}