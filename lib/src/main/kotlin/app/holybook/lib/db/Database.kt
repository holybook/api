package app.holybook.lib.db

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Database {

    private val dataSource = HikariDataSource()
    private val log: Logger = LoggerFactory.getLogger("db")

    fun init(jdbcUrl: String) {
        dataSource.jdbcUrl = jdbcUrl
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

    suspend fun <R> transactionSuspending(body: suspend Connection.() -> R): R {
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
