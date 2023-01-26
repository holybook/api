package app.holybook.import

import app.holybook.api.db.Database
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>) = runBlocking {
    val options = Options()
    options.addOption(
        "c",
        "recreate-db",
        false,
        "Recreate the database and populate it from scratch."
    )
    options.addOption("h", "host", true, "Database host")
    options.addOption("p", "port", true, "Database port")
    options.addOption("d", "database", true, "Database name")
    options.addOption("u", "user", true, "Database username")
    options.addOption("pwd", "password", false, "Use password")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    val jdbcUrl = getJdbcUrl(
        host = cmd.getOptionValue("h", "127.0.0.1"),
        port = cmd.getOptionValue("p", "5432"),
        database = cmd.getOptionValue("d", "holybook"),
        user = cmd.getOptionValue("u", "server"),
        usePassword = cmd.hasOption("pwd")
    )
    Database.init(jdbcUrl)

    if (cmd.hasOption("c")) {
        resetDatabase()
    }
    fetchAndImportIndex()
}

fun getJdbcUrl(
    host: String,
    port: String,
    database: String,
    user: String,
    usePassword: Boolean
): String {
    val passwordQuery = if (usePassword) {
        "&password=${readPassword()}"
    } else {
        ""
    }
    return "jdbc:postgresql://$host:$port/$database?user=$user$passwordQuery"
}

private fun readPassword(): String {
    val console = System.console()
    if (console == null) {
        print("Password: ")
        return readln()
    }
    return console.readPassword("Password: ").toString()
}