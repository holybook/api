package app.holybook.api.config

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName

/**
 * Provides access to secrets stored in Google Cloud Secret Manager.
 *
 * This object manages the retrieval of secrets by utilizing the Google Cloud SecretManagerServiceClient.
 * It is designed to be used in scenarios where secure and centralized storage of sensitive configuration
 * or credentials is required. The retrieval process ensures that the latest version of the secret is fetched.
 *
 * Functions:
 * - getSecret(secretId: String): Fetches the latest version of the specified secret from Google Cloud Secret Manager.
 *
 * Usage of this object abstracts the details of interacting with Secret Manager and provides a simple API for
 * accessing secrets.
 */
object SecretsProvider {

  private val client by lazy {
    SecretManagerServiceClient.create()
  }

  fun getSecret(secretId: String): String {
    val secretVersionName =
      SecretVersionName.of("965613074283", secretId, "latest")
    val response = client.accessSecretVersion(secretVersionName)
    return response.payload.data.toStringUtf8()
  }

}