package com.example.presentations.auth

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
private val hashKey = config.property("jwt.secret").getString().toByteArray()
private val hmacKey = SecretKeySpec(hashKey, "HmcSHA1")

fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}