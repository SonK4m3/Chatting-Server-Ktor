package com.example.models.tables

import org.jetbrains.exposed.sql.Table

object MessageTable: Table("message") {
    val id = integer("id").entityId().autoIncrement()
    val fromId = integer("fromId").references(UserTable.id)
    val toId = integer("toId").references(UserTable.id)
    val sendDT = long("sendDT")
    val read = bool("read")
    val readDT = long("readDT")
    val message = text("message")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}