package app.holybook.api.models

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Books : IntIdTable() {
    val title = varchar("title", 256)
}