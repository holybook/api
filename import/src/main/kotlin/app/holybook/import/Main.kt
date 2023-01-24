package app.holybook.import

import app.holybook.api.db.Database
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val conf: Config = ConfigFactory.load()
    val password =
        System.console()?.readPassword("Password: ") ?: readLine()
    Database.init(conf.getJdbcUrl(password.toString()))
    fetchAndImportIndex()
}

fun Config.getJdbcUrl(password: String): String {
    val host = getString("storage.hostName")
    val port = getString("storage.port")
    val db = getString("storage.dbName")
    val user = getString("storage.userName")
    return "jdbc:postgresql://$host:$port/$db?user=$user&password=$password"
}