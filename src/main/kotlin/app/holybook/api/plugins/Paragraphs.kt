package app.holybook.api.plugins

import app.holybook.api.models.Paragraphs
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

// fun Routing.configureParagraphs() {
//   get("books/{id}/paragraphs") { getParagraphs() }
// }
//
// suspend fun PipelineContext<Unit, ApplicationCall>.getParagraphs() {
//   val bookId = call.parameters["id"]
//
//   if (bookId == null) {
//     call.respond(HttpStatusCode.NotFound)
//     return
//   }
//
//   val language = call.request.queryParameters["lang"] ?: "en"
//   val startIndex = call.request.queryParameters["start"]?.toInt()
//   val endIndex = call.request.queryParameters["end"]?.toInt()
//
//   val paragraphs = transaction {
//     val query =
//         Paragraphs.select { (Paragraphs.bookId eq bookId) and (Paragraphs.language eq language) }
//     if (startIndex != null) {
//       query.andWhere { Paragraphs.index greaterEq startIndex }
//     }
//     if (endIndex != null) {
//       query.andWhere { Paragraphs.index lessEq endIndex }
//     }
//     query.orderBy(Paragraphs.index).map { Paragraph(it[Paragraphs.index], it[Paragraphs.text]) }
//   }
//   call.respond(paragraphs)
// }

@Serializable data class Paragraph(val index: Int, val text: String)
