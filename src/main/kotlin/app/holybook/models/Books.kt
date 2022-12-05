package app.holybook.models

import org.jetbrains.exposed.sql.Table

object Books : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 256)

    override val primaryKey = PrimaryKey(id)
}