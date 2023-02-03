package app.holybook.lib.models

import app.holybook.lib.serialization.DateSerializer
import java.time.LocalDate
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName

@Serializable
data class BookContent(
  val title: String,
  val author: String,
  @Serializable(with = DateSerializer::class)
  val publishedAt: LocalDate?,
  @XmlChildrenName("par")
  val paragraphs: List<Paragraph>
)
