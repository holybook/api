package app.holybook.index

import app.holybook.index.IndexLogger.log
import app.holybook.lib.models.toBookContent
import app.holybook.lib.parsers.readDocument
import app.holybook.lib.path.PathExtensions.listFilesRecursive
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.relativeTo

class Indexer {
  private val indexedBooks = mutableMapOf<String, BookMetadata>()

  fun indexDirectoryRecursive(path: Path) {
    path.listFilesRecursive().forEach { indexFile(path, it) }
  }

  private fun indexFile(base: Path, path: Path) {
    log.info("Indexing ${path.toAbsolutePath()}")
    val book = path.inputStream().readDocument().toBookContent()

    val bookPath = path.relativeTo(base).toString()
    indexedBooks[book.id] = BookMetadata(book.title, bookPath)
  }
}

@Serializable data class Index(val metadata: Map<String, BookMetadata>)

@Serializable data class BookMetadata(val title: String, val path: String)
