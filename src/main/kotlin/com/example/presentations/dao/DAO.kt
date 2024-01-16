package com.example.presentations.dao

import org.jetbrains.exposed.sql.ResultRow

interface DAO<T> {
    fun resultRowToClass(row: ResultRow): T
    suspend fun readAll(): List<T>
    suspend fun read(id: Int): T?
    suspend fun create(new: T): T?
    suspend fun delete(id: Int): Boolean
    suspend fun update(id: Int, new: T): Boolean
}