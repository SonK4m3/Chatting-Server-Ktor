package com.example.utils

import java.time.LocalDateTime

fun dateTimeToLong(): Long {
    val dateTime = LocalDateTime.now()

    return dateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
}