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
    private val provider: String = "GENERIC"
) : AuthProvider {
    
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
            return try {
                val claims = parseJwt(token)
                AuthResult.Success(
                    AuthUser(
                        id = claims[userClaim] as? String ?: "unknown",
                        email = claims[emailClaim] as? String ?: (claims["preferred_username"] as? String ?: "unknown"),
                        name = claims[nameClaim] as? String ?: (claims["preferred_username"] as? String ?: "unknown"),
                        roles = extractRoles(claims)
                    )
                )
            } catch (e: Exception) {
                AuthResult.Failure("Invalid OIDC context ($provider): ${e.message}")
            }
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
                // Try common claims
                val roles = (claims["roles"] as? List<*>) ?: (claims["groups"] as? List<*>) ?: (claims["scp"] as? String)?.split(" ") ?: emptyList<Any>()
                roles.map { it.toString() }
            }
        }
    }
}
