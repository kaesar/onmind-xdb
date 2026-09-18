package co.onmind.auth

import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import co.onmind.util.JsonMapper
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import java.util.Base64

class OIDCPlug(
    private val serverUrl: String? = null,
    private val realm: String? = null,
    private val clientId: String,
    private val userClaim: String = "sub",
    private val emailClaim: String = "email",
    private val nameClaim: String = "name",
    private val rolesClaim: String? = null, // if null, try default mappings
    private val provider: String = "GENERIC",
    /** HMAC shared secret for HS256 JWT verification (OnMind-UID default). */
    private val sharedSecret: String? = null,
    /** Expected `iss` claim (OnMind-UID `uid.issuer`). */
    private val expectedIssuer: String? = null,
    /** Expected `aud` claim. Null = no audience check (eXpress default). */
    private val expectedAudience: String? = null,
    /**
     * JWKS URL for RS256 verification (Keycloak, Entra ID, ...).
     * If null and provider is KEYCLOAK (or OIDC with realm), it tries derived as
     * `{serverUrl}/realms/{realm}/protocol/openid-connect/certs`.
     */
    private val jwksUrl: String? = null
) : AuthProvider {

    companion object {
        /** Derive the Keycloak certs endpoint, or null when it cannot be derived. */
        fun defaultJwksUrl(serverUrl: String?, realm: String?, provider: String): String? {
            if (serverUrl.isNullOrBlank() || realm.isNullOrBlank()) return null
            if (!provider.equals("KEYCLOAK", ignoreCase = true) &&
                !provider.equals("OIDC", ignoreCase = true)) return null
            return serverUrl.trimEnd('/') + "/realms/" + realm.trim('/') + "/protocol/openid-connect/certs"
        }
    }

    private val effectiveJwksUrl: String? =
        jwksUrl?.takeIf { it.isNotBlank() } ?: defaultJwksUrl(serverUrl, realm, provider)

    private var legacyWarningLogged = false
    
    private val json = JsonMapper.instance

    override fun authenticate(request: Request): AuthResult {
        // 1. Intentar obtener identidad desde cabeceras (Proxy mode)
        val remoteUser = request.header("X-Auth-User") ?: request.header("X-Proxy-User")
        if (remoteUser != null) {
            val email = request.header("X-Auth-Email") ?: request.header("X-Proxy-Email") ?: remoteUser
            val name = request.header("X-Auth-Name") ?: request.header("X-Proxy-Name") ?: remoteUser
            val roles = request.header("X-Auth-Roles")?.split(",") ?: emptyList()
            
            return AuthResult.Success(
                AuthUser(id = remoteUser, email = email, name = name, roles = roles)
            )
        }

        // 2. Intentar obtener desde Bearer Token (JWT mode)
        val authHeader = request.header("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ", ignoreCase = true)) {
            val token = authHeader.substring(7)

            // Real verification when configured (JWKS RS256 first, then HS256 secret).
            // Without either, keep the legacy eXpress behaviour (decode only).
            val claims: Map<String, Any> = if (effectiveJwksUrl != null) {
                JwksValidator.verify(token, effectiveJwksUrl, issuer = expectedIssuer, audience = expectedAudience)
                    ?: return AuthResult.Failure("Invalid JWT signature, issuer, audience or expired token ($provider, JWKS)")
            } else if (!sharedSecret.isNullOrEmpty()) {
                JwtValidator.verify(token, sharedSecret, issuer = expectedIssuer, audience = expectedAudience)
                    ?: return AuthResult.Failure("Invalid JWT signature, issuer or expired token ($provider)")
            } else {
                // Legacy: decode only (no signature verification). Prefer configuring
                // auth.oidc.jwks_url (RS256) or auth.jwt.secret (HS256).
                if (!legacyWarningLogged) {
                    legacyWarningLogged = true
                    println("[WARN] OIDCPlug($provider): no JWKS nor shared secret configured; " +
                        "JWT signature is NOT verified (eXpress legacy mode). " +
                        "Set auth.oidc.jwks_url or auth.jwt.secret in onmind.ini for production.")
                }
                try {
                    parseJwt(token)
                } catch (e: Exception) {
                    return AuthResult.Failure("Invalid OIDC context ($provider): ${e.message}")
                }
            }

            return AuthResult.Success(
                AuthUser(
                    id = claims[userClaim] as? String ?: "unknown",
                    email = claims[emailClaim] as? String ?: (claims["preferred_username"] as? String ?: "unknown"),
                    name = claims[nameClaim] as? String ?: (claims["preferred_username"] as? String ?: "unknown"),
                    roles = extractRoles(claims)
                )
            )
        }

        return AuthResult.Failure("No authentication found for $provider (missing headers or Bearer token)")
    }

    override fun filter() = Filter { next ->
        { request ->
            when (val result = authenticate(request)) {
                is AuthResult.Success -> {
                    next(request.header("X-Auth-User", result.user.id))
                }
                is AuthResult.Failure -> Response(Status.UNAUTHORIZED).body(result.reason)
            }
        }
    }

    private fun parseJwt(token: String): Map<String, Any> {
        val parts = token.split(".")
        if (parts.size < 2) throw IllegalArgumentException("Malformed JWT")
        
        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        @Suppress("UNCHECKED_CAST")
        return json.readValue(payload, Map::class.java) as Map<String, Any>
    }

    private fun extractRoles(claims: Map<String, Any>): List<String> {
        if (rolesClaim != null) {
            return (claims[rolesClaim] as? List<*>)?.map { it.toString() } ?: emptyList()
        }

        // Default mappings based on provider
        return when (provider.uppercase()) {
            "KEYCLOAK" -> {
                val resourceAccess = claims["resource_access"] as? Map<*, *>
                val clientAccess = resourceAccess?.get(clientId) as? Map<*, *>
                val clientRoles = (clientAccess?.get("roles") as? List<*>)?.map { it.toString() } ?: emptyList()
                val realmAccess = claims["realm_access"] as? Map<*, *>
                val realmRoles = (realmAccess?.get("roles") as? List<*>)?.map { it.toString() } ?: emptyList()
                (clientRoles + realmRoles).distinct()
            }
            "ENTRAID" -> {
                val roles = (claims["roles"] as? List<*>)?.map { it.toString() } ?: emptyList()
                val groups = (claims["groups"] as? List<*>)?.map { it.toString() } ?: emptyList()
                (roles + groups).distinct()
            }
            else -> {
                // Try common claims (incl. Cognito-style groups from OnMind-UID)
                val cognitoGroups = (claims["cognito:groups"] as? List<*>)?.map { it.toString() } ?: emptyList()
                val genericRoles = (claims["roles"] as? List<*>) ?: (claims["groups"] as? List<*>) ?: (claims["scp"] as? String)?.split(" ") ?: emptyList<Any>()
                (cognitoGroups + genericRoles.map { it.toString() }).distinct()
            }
        }
    }
}
