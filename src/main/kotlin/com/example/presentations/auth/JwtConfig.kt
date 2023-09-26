package com.example.presentations.auth

import com.typesafe.config.ConfigFactory


object JwtConfig {
    val secret = System.getenv().get("jwt.secret")
}