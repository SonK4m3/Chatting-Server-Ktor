package com.example.presentations.dao

interface DAO<T> {
    suspend fun getAll(): List<T>
    suspend fun findById(id: Int): T?
    suspend fun addNew(new: T): T?
    suspend fun removeById(id: Int): Boolean
    suspend fun edit(id: Int, new: T): Boolean
}