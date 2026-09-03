package co.onmind.auth

import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class CognitoPlug(
    private val region: String,
    private val userPoolId: String,
    private val clientId: String,
    private val sharedSecret: String? = null,
    private val expectedIssuer: String? = null
) : AuthProvider {
    override fun authenticate(request: Request): AuthResult {
        val token = request.header("Authorization")?.removePrefix("Bearer ")
            ?: return AuthResult.Failure("No Authorization header")

        return try {
            val claims = validateToken(token)
            AuthResult.Success(
                AuthUser(
                    id = claims["sub"] as String,
                    email = claims["email"] as String,
                    name = claims["name"] as? String ?: claims["email"] as String,
                    roles = extractGroups(claims)
                )
            )
        } catch (e: Exception) {
            AuthResult.Failure("Invalid token: ${e.message}")
        }
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

    private fun validateToken(token: String): Map<String, Any> {
        val secret = sharedSecret
        if (secret.isNullOrEmpty()) {
            throw IllegalArgumentException("auth.cognito.shared_secret required to validate OnMind-UID JWTs")
        }
        val claims = JwtValidator.verify(token, secret, issuer = expectedIssuer, audience = clientId, leewaySeconds = 30)
            ?: throw IllegalArgumentException("Invalid JWT signature, issuer, audience or expired token")
        if (claims["token_use"] != "access") {
            throw IllegalArgumentException("Token is not an access token")
        }
        return claims
    }

    private fun extractGroups(claims: Map<String, Any>): List<String> {
        val cognitoGroups = (claims["cognito:groups"] as? List<*>)?.map { it.toString() } ?: emptyList()
        val username = claims["username"] as? String
        val status = claims["user_status"] as? String
        val out = cognitoGroups.toMutableList()
        // Mirror OnMind-UID: a CONFIRMED user gets the implicit "user" role.
        if (status == "CONFIRMED") out.add("user")
        return out.distinct()
    }
}
