package app.holybook.api.models

import org.jetbrains.exposed.dao.id.IntIdTable

object Books : IntIdTable() {
    val author = varchar("author", 256)
}