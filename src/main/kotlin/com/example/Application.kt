package com.example

import com.example.presentations.repositories.DatabaseFactory
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@OptIn(KtorExperimentalLocationsAPI::class)
fun Application.module() {
    DatabaseFactory.init(environment.config)

    install(DefaultHeaders)
    install(Locations)
    install(Resources)
    install(CORS) {
        anyHost()
        allowCredentials = true
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    configAuthenticationJWT()
    configSerializer()
    configureSockets()
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "An internal server error occurred {$cause}")
        }
    }
    configureRouting()
//    configureTemplating()
}
