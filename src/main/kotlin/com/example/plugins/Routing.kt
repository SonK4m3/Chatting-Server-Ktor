package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.presentations.auth.hash
import com.example.presentations.routes.userRoutes
import com.example.presentations.routes.messageRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureRouting() {
    val secret = environment.config.property("jwt.secret").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val issuer = environment.config.property("jwt.issuer").getString()

    val hashFunction = { s: String -> hash(s) }
    val generateJWT = { s: String ->
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("email", s)
            .withExpiresAt(Date(System.currentTimeMillis() + 30000))
            .sign(Algorithm.HMAC256(secret))
    }

    routing {

        userRoutes(generateJWT, hashFunction)

        authenticate("auth-jwt") {
            get("/auth-hello") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respond(HttpStatusCode.OK, "Hello, $email! Token is expired at $expiresAt ms.")
            }

            messageRoute()
        }

//        route("/auth") {
//            post("/login") {
//                val user = call.receive<User>()
//                val token = generateJWT(user.email)
//
//                call.respond(hashMapOf("token" to token))
//            }
//        }
//
//        route("/test") {
//            get("/user") {
//                val email = call.request.queryParameters["email"]!!
//                val password = call.request.queryParameters["password"]!!
//                val username = call.request.queryParameters["username"]!!
//
//                val user = User(0, email, hashFunction(password), username)
//                val token = JWT.create()
//                    .withSubject("NoteAuthentication")
//                    .withAudience(audience)
//                    .withIssuer(issuer)
//                    .withClaim("email", user.email)
//                    .withExpiresAt(Date(System.currentTimeMillis() + 30000))
//                    .sign(Algorithm.HMAC256(secret))
//
//                call.respond(hashMapOf("token" to token))
//            }
//        }
//
//        route("/hello") {
//            get {
//                call.respond("Hello, world!")
//            }
//        }

//        webRoute()
    }
}
