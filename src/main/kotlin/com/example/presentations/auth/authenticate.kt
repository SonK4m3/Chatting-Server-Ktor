package com.example.presentations.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
private val hashKey = config.property("jwt.secret").getString().toByteArray()
val _audience = config.property("jwt.audience").getString()
val _issuer = config.property("jwt.issuer").getString()
val _realm = config.property("jwt.realm").getString()

private val hmacKey = SecretKeySpec(hashKey, "HmcSHA1")

fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

fun verifyJWTToken(token: String): DecodedJWT? {
    try {
        val jwt = JWT.decode(token)

        return jwt
    } catch (e: JWTVerificationException) {
        return null
    }
}