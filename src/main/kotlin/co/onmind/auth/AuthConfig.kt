package co.onmind.auth

import java.util.Properties

enum class AuthType {
    BASIC, AUTHELIA, COGNITO
}

data class AuthConfig(
    val enabled: Boolean = true,
    val type: AuthType = AuthType.BASIC,
    val basicUser: String = "admin",
    val basicPass: String = "admin",
    val autheliaUrl: String? = null,
    val cognitoRegion: String? = null,
    val cognitoUserPoolId: String? = null,
    val cognitoClientId: String? = null
) {
    fun createProvider(): AuthProvider = when {
        !enabled -> NoAuthProvider()
        type == AuthType.BASIC -> BasicAuthProvider(basicUser, basicPass)
        type == AuthType.AUTHELIA -> AutheliaProvider(autheliaUrl ?: error("auth.authelia.url required"))
        type == AuthType.COGNITO -> CognitoProvider(
            cognitoRegion ?: error("auth.cognito.region required"),
            cognitoUserPoolId ?: error("auth.cognito.user_pool_id required"),
            cognitoClientId ?: error("auth.cognito.client_id required")
        )
        else -> BasicAuthProvider(basicUser, basicPass)
    }
    
    companion object {
        fun fromConfig(config: Properties): AuthConfig {
            val enabled = config.getProperty("auth.enabled", "true") == "true"
            val type = config.getProperty("auth.type", "BASIC").uppercase().let { AuthType.valueOf(it) }
            return AuthConfig(
                enabled = enabled,
                type = type,
                basicUser = config.getProperty("auth.basic.user", "admin"),
                basicPass = config.getProperty("auth.basic.pass", "admin"),
                autheliaUrl = config.getProperty("auth.authelia.url"),
                cognitoRegion = config.getProperty("auth.cognito.region"),
                cognitoUserPoolId = config.getProperty("auth.cognito.user_pool_id"),
                cognitoClientId = config.getProperty("auth.cognito.client_id")
            )
        }
    }
}
