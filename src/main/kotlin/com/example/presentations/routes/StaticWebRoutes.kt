package com.example.presentations.routes

import com.example.models.articles
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.io.File

fun Route.webRoute() {
    get("/video/{fileName}") {
        val fileName =
            call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing file name")
        val file = File("D:\\Documents\\video", fileName)

        if (file.exists() && file.extension == "mp4") {
            file.inputStream().use { inputStream ->
                call.respondOutputStream() {
                    inputStream.copyTo(this)
                }
            }
        } else {
            call.respond(HttpStatusCode.NotFound, "File not found or not a video")
        }
    }
}