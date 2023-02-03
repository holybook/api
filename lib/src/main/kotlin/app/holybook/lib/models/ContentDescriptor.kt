package app.holybook.lib.models

import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader
import kotlinx.serialization.Serializable

/** Describes the location of an original document containing the data of a book. */
@Serializable data class ContentDescriptor(val id: String, val language: String, val url: String)

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

fun String.readContentDescriptor() : ContentDescriptor {
  val parts = split(' ')
  return ContentDescriptor(
    id = parts[1],
    language = parts[0],
    url = parts[2]
  )
}
