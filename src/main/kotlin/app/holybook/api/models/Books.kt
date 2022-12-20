package app.holybook.api.models

import org.jetbrains.exposed.dao.id.IdTable

object Books : IdTable<String>() {
    override val id = varchar("id", 32).entityId()
    val author = varchar("author", 256)

    override val primaryKey = PrimaryKey(id)
}