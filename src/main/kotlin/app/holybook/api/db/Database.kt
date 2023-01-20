package app.holybook.api.db

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import io.ktor.util.logging.Logger
import java.sql.Connection
import java.sql.SQLException

object Database {

  private val dataSource = HikariDataSource()
  private var log: Logger? = null

  fun init(config: ApplicationConfig, log: Logger) {
    this.log = log
    dataSource.jdbcUrl = config.getJdbcUrl()
    dataSource.username = "server"
  }

  fun <R> transaction(body: Connection.() -> R): R {
    val connection = dataSource.connection
    return try {
      connection.autoCommit = false
      val result = connection.body()
      connection.commit()
      result
    } catch (e: SQLException) {
      log?.error("transaction failed", e)
      connection.rollback()
      throw e
    } finally {
      connection.close()
    }
  }

  fun ApplicationConfig.getJdbcUrl(): String {
    val host = property("storage.hostName").getString()
    val port = property("storage.port").getString()
    val db = property("storage.dbName").getString()
    val user = property("storage.userName").getString()
    val passwordParameter = propertyOrNull("storage.password")?.getString().let {
      if (it == null || it.isEmpty()) {
        null
      } else {
        "&password=$it"
      }
    } ?: ""
    return "jdbc:postgresql://$host:$port/$db?user=$user$passwordParameter"
  }
}
