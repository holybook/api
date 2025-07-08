package app.holybook.import.sources.model

import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.parsers.ContentParser
import io.ktor.http.Url

class SourceDescriptor(
  val url: Url,
  val fileName: String,
  val parser: ContentParser<List<ContentDescriptor>>,
)
