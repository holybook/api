package app.holybook.lib.models

import java.io.*
import kotlinx.serialization.Serializable

/** Describes the location of an original document containing the data of a book. */
@Serializable
data class ContentDescriptor(
  val id: String,
  val language: String,
  val authorCode: String,
  val url: String
)

fun readContentDescriptors(input: InputStream): List<ContentDescriptor> {
  val resultList = mutableListOf<ContentDescriptor>()
  val reader = LineNumberReader(InputStreamReader(input))
  var line: String? = reader.readLine()
  while (line != null) {
    resultList.add(line.readContentDescriptor())
    line = reader.readLine()
  }
  return resultList
}

fun String.readContentDescriptor(): ContentDescriptor {
  val parts = split(' ')
  return ContentDescriptor(
    language = parts[0],
    authorCode = parts[1],
    id = parts[2],
    url = parts[3]
  )
}

fun Writer.writeDescriptor(descriptor: ContentDescriptor) {
  write("${descriptor.language} ${descriptor.authorCode} ${descriptor.id} ${descriptor.url}\n")
}
