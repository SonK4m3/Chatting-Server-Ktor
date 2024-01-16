package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.Payload
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configAuthenticationJWT() {
    val _secret = environment.config.property("jwt.secret").getString()
    val _audience = environment.config.property("jwt.audience").getString()
    val _issuer = environment.config.property("jwt.issuer").getString()
    val _realm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = _realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(_secret))
                    .withAudience(_audience)
                    .withIssuer(_issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}