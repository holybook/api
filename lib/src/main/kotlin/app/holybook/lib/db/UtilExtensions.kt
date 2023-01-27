package app.holybook.lib.db

import java.sql.ResultSet
import kotlin.collections.mutableListOf

fun <T> ResultSet.map(body: ResultSet.() -> T): List<T> {
  val results = mutableListOf<T>()
  while (next()) {
    results.add(body())
  }
  return results
}