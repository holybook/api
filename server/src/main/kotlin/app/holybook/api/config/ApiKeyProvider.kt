package app.holybook.api.config

import app.holybook.api.config.ApplicationConfigExt.getSecretManagerProjectId
import app.holybook.api.config.ApplicationConfigExt.getSecretManagerSecretId
import app.holybook.api.config.ApplicationConfigExt.getSecretManagerSecretVersion
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import io.ktor.server.config.*
import org.slf4j.LoggerFactory

object ApiKeyProvider {
  private val log = LoggerFactory.getLogger(ApiKeyProvider::class.java)

  fun getApiKey(config: ApplicationConfig): String {
    // Check if API key is available in environment variable
    val apiKeyFromEnv = System.getenv("GOOGLE_API_KEY")
    if (!apiKeyFromEnv.isNullOrBlank()) {
      log.info("Using API key from environment variable")
      return apiKeyFromEnv
    }

    // If not available in environment, get it from Google Cloud Secret Manager
    log.info("API key not found in environment, retrieving from Secret Manager")

    val projectId = config.getSecretManagerProjectId()
    val secretId = config.getSecretManagerSecretId()
    val secretVersion = config.getSecretManagerSecretVersion()

    return SecretManagerServiceClient.create().use { client ->
      val secretVersionName =
        SecretVersionName.of(projectId, secretId, secretVersion)
      val response = client.accessSecretVersion(secretVersionName)
      response.payload.data.toStringUtf8()
    }
  }
}
