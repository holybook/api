package app.holybook.lib.translation

import kotlinx.serialization.Serializable

@Serializable
data class ParagraphAnnotation(
  val bookId: String,
  val index: Int,
  val number: Int? = null,
  val title: String,
)

@Serializable
data class ParagraphWithAnnotation(
  val annotation: ParagraphAnnotation? = null,
  val text: String
)