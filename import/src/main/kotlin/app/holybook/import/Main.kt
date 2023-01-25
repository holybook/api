package app.holybook.import

import app.holybook.api.db.Database
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>) = runBlocking {
  val conf: Config = ConfigFactory.load()
  val password =
    System.console()?.readPassword("Password: ") ?: {
      print("Password: ")
      readLine()
    }
  Database.init(conf.getJdbcUrl(password.toString()))

  val options = Options()
  options.addOption("c",
                    "recreate-db",
                    false,
                    "Recreate the database and populate it from scratch.")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  if (cmd.hasOption("c")) {
    resetDatabase()
  }
  fetchAndImportIndex()
}

fun Config.getJdbcUrl(password: String): String {
  val host = getString("storage.hostName")
  val port = getString("storage.port")
  val db = getString("storage.dbName")
  val user = getString("storage.userName")
  return "jdbc:postgresql://$host:$port/$db?user=$user&password=$password"
}