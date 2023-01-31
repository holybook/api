package app.holybook.import.model

import app.holybook.import.common.ContentParser
import io.ktor.http.Url

class SourceDescriptor(
  val url: Url,
  val fileName: String,
  val parser: ContentParser<List<ContentDescriptor>>
)
