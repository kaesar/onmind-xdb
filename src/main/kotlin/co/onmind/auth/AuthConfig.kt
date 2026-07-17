package co.onmind.auth

import co.onmind.trait.AuthProvider
import java.util.Properties

enum class AuthType {
    BASIC, AUTHELIA, COGNITO, KEYCLOAK, ENTRAID, OIDC, OTPMAIL
}

data class AuthConfig(
    val enabled: Boolean = true,
    val type: AuthType = AuthType.BASIC,
    val basicUser: String = "admin",
    val basicPass: String = "admin",
    val autheliaUrl: String? = null,
    val cognitoRegion: String? = null,
    val cognitoUserPoolId: String? = null,
    val cognitoClientId: String? = null,
    val oidcUrl: String? = null,
    val oidcRealm: String? = null,
    val oidcClientId: String? = null,
    val oidcUserClaim: String? = null,
    val oidcEmailClaim: String? = null,
    val oidcRolesClaim: String? = null,
    val otpSmtpHost: String = "localhost",
    val otpSmtpPort: String = "1025",
    val otpSmtpUser: String = "",
    val otpSmtpPass: String = "",
    val otpFromEmail: String = "xdb@localhost",
    val otpSessionKey: String = "change-me-otp-session-key",
    val otpAutoRegister: Boolean = true
) {
    fun createProvider(): AuthProvider = when {
        !enabled -> NoAuthPlug()
        type == AuthType.BASIC -> BasicAuthPlug(basicUser, basicPass)
        type == AuthType.AUTHELIA -> AutheliaPlug(autheliaUrl ?: error("auth.authelia.url required"))
        type == AuthType.COGNITO -> CognitoPlug(
            cognitoRegion ?: error("auth.cognito.region required"),
            cognitoUserPoolId ?: error("auth.cognito.user_pool_id required"),
            cognitoClientId ?: error("auth.cognito.client_id required")
        )
        type == AuthType.KEYCLOAK -> OIDCPlug(
            serverUrl = oidcUrl,
            realm = oidcRealm,
            clientId = oidcClientId ?: error("auth.oidc.client_id required"),
            provider = "KEYCLOAK"
        )
        type == AuthType.ENTRAID -> OIDCPlug(
            serverUrl = oidcUrl,
            clientId = oidcClientId ?: error("auth.oidc.client_id required"),
            provider = "ENTRAID"
        )
        type == AuthType.OIDC -> OIDCPlug(
            serverUrl = oidcUrl,
            realm = oidcRealm,
            clientId = oidcClientId ?: error("auth.oidc.client_id required"),
            userClaim = oidcUserClaim ?: "sub",
            emailClaim = oidcEmailClaim ?: "email",
            rolesClaim = oidcRolesClaim,
            provider = "OIDC"
        )
        type == AuthType.OTPMAIL -> OTPMailPlug(
            smtpHost = otpSmtpHost,
            smtpPort = otpSmtpPort,
            smtpUser = otpSmtpUser,
            smtpPass = otpSmtpPass,
            fromEmail = otpFromEmail,
            sessionKey = otpSessionKey,
            autoRegister = otpAutoRegister
        )
        else -> BasicAuthPlug(basicUser, basicPass)
    }
    
    companion object {
        fun fromConfig(config: Properties): AuthConfig {
            val enabled = config.getProperty("auth.enabled", "true") == "true"
            val typeStr = config.getProperty("auth.type", "BASIC").uppercase()
            val type = try { AuthType.valueOf(typeStr) } catch(e: Exception) { AuthType.BASIC }
            
            val basicUser = config.getProperty("auth.basic.user", "YWRtaW4=").let {
                if (it.matches(Regex("^[A-Za-z0-9+/=]+$"))) {
                    try { String(java.util.Base64.getDecoder().decode(it)) } catch(e: Exception) { it }
                } else it
            }
            val basicPass = config.getProperty("auth.basic.pass", "YWRtaW4=").let {
                if (it.matches(Regex("^[A-Za-z0-9+/=]+$"))) {
                    try { String(java.util.Base64.getDecoder().decode(it)) } catch(e: Exception) { it }
                } else it
            }
            
            return AuthConfig(
                enabled = enabled,
                type = type,
                basicUser = basicUser,
                basicPass = basicPass,
                autheliaUrl = config.getProperty("auth.authelia.url"),
                cognitoRegion = config.getProperty("auth.cognito.region"),
                cognitoUserPoolId = config.getProperty("auth.cognito.user_pool_id"),
                cognitoClientId = config.getProperty("auth.cognito.client_id"),
                oidcUrl = config.getProperty("auth.oidc.url") ?: config.getProperty("auth.keycloak.url"),
                oidcRealm = config.getProperty("auth.oidc.realm") ?: config.getProperty("auth.keycloak.realm"),
                oidcClientId = config.getProperty("auth.oidc.client_id") ?: config.getProperty("auth.keycloak.client_id"),
                oidcUserClaim = config.getProperty("auth.oidc.user_claim"),
                oidcEmailClaim = config.getProperty("auth.oidc.email_claim"),
                oidcRolesClaim = config.getProperty("auth.oidc.roles_claim"),
                otpSmtpHost = config.getProperty("auth.otp.smtp_host", "localhost"),
                otpSmtpPort = config.getProperty("auth.otp.smtp_port", "1025"),
                otpSmtpUser = config.getProperty("auth.otp.smtp_user", ""),
                otpSmtpPass = config.getProperty("auth.otp.smtp_pass", ""),
                otpFromEmail = config.getProperty("auth.otp.from", "xdb@localhost"),
                otpSessionKey = config.getProperty("auth.otp.session_key", "change-me-otp-session-key"),
                otpAutoRegister = config.getProperty("auth.otp.auto_register", "true") == "true"
            )
        }
    }
}
