package app.holybook.api.config

import io.ktor.server.config.ApplicationConfig

/**
 * Provides extension functions for retrieving configuration properties from [ApplicationConfig].
 *
 * Each function in this object is an extension to the [ApplicationConfig] class and is designed to
 * fetch specific configuration values required for application functionality, such as database connection
 * settings or integration with external services like Google Cloud Secret Manager.
 *
 * Extensions:
 * - getJdbcUrl(): Retrieves the JDBC URL for database connection.
 * - getSecretManagerProjectId(): Retrieves the project ID for Google Cloud Secret Manager.
 * - getSecretManagerSecretId(): Retrieves the secret ID for Google Cloud Secret Manager.
 * - getSecretManagerSecretVersion(): Retrieves the secret version for Google Cloud Secret Manager.
 */
object ApplicationConfigExt {
  fun ApplicationConfig.getJdbcUrl(): String {
    return getPropertyFromSecret("storage.jdbcUrl")
  }

  fun ApplicationConfig.getGeminiApiKey(): String {
    return getPropertyFromSecret("ai.geminiApiKey")
  }

  private fun ApplicationConfig.getPropertyFromSecret(propertyName: String): String {
    val configValue = property(propertyName).getString()
    if (configValue.startsWith("secret:")) {
      val secretName = configValue.substringAfter("secret:")
      return SecretsProvider.getSecret(secretName)
    }

    return configValue
  }
}