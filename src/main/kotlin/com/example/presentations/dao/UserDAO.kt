package com.example.presentations.dao

import com.example.models.User
import com.example.models.tables.UserTable
import org.jetbrains.exposed.sql.ResultRow
import com.example.presentations.repositories.DatabaseFactory.dbQuery
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class UserDAO : DAO<User> {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UserTable.id],
        email = row[UserTable.email],
        hashPassword = row[UserTable.hashPassword],
        username = row[UserTable.username]
    )

    override suspend fun getAll(): List<User> {
        return dbQuery {
            UserTable.selectAll().map(::resultRowToUser)
        }
    }

    override suspend fun findById(id: Int): User? {
        return dbQuery {
            UserTable
                .select { UserTable.id eq id }
                .map(::resultRowToUser)
                .singleOrNull()
        }
    }

    override suspend fun removeById(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun edit(id: Int, new: User): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun addNew(new: User): User? {
        return dbQuery {
            UserTable.insert {
                it[UserTable.email] = new.email
                it[UserTable.hashPassword] = new.hashPassword
                it[UserTable.username] = new.username
            }.resultedValues?.map(::resultRowToUser)?.singleOrNull()
        }
    }

    suspend fun findByEmail(email: String): User? {
        return dbQuery {
            UserTable
                .select { UserTable.email eq email }
                .map(::resultRowToUser)
                .singleOrNull()
        }
    }
}