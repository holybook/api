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
    dataSource.jdbcUrl = config.property("storage.jdbcURL").getString()
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
}
