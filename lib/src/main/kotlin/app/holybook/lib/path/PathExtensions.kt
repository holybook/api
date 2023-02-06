package app.holybook.lib.path

import app.holybook.lib.models.readContentDescriptors
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlinx.coroutines.runBlocking

object PathExtensions {

  fun Path.listFilesRecursive(): List<Path> {
    val result = mutableListOf<Path>()
    visitPath(this, result)
    return result
  }

  private fun visitPath(path: Path, list: MutableList<Path>) {
    if (path.isDirectory()) {
      Files.list(path).forEach { visitPath(it, list) }
      return
    }

    list.add(path)
  }

}