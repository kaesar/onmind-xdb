package co.onmind.auth

import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import java.util.Locale
import java.util.Locale.getDefault

interface AuthProvider {
    fun authenticate(request: Request): AuthResult
    fun filter(): Filter
}

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Failure(val reason: String) : AuthResult()
}

data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val roles: List<String> = emptyList()
)

class NoAuthProvider : AuthProvider {
    override fun authenticate(request: Request) = AuthResult.Success(
        AuthUser(id = "admin", email = "admin@local", name = "Admin", roles = listOf("ADMIN"))
    )
    
    override fun filter() = Filter { next -> { request -> next(request) } }
}

class BasicAuthProvider(private val user: String, private val pass: String) : AuthProvider {
    override fun authenticate(request: Request): AuthResult {
        val auth = request.header("Authorization")?.removePrefix("Basic ")
            ?: return AuthResult.Failure("No Authorization header")
        
        val decoded = String(java.util.Base64.getDecoder().decode(auth))
        val (reqUser, reqPass) = decoded.split(":", limit = 2)
        
        return if (reqUser == user && reqPass == pass) {
            AuthResult.Success(AuthUser(id = user, email = "$user@local", name = user.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                getDefault()
            ) else it.toString() }, roles = listOf("ADMIN")))
        } else {
            AuthResult.Failure("Invalid credentials")
        }
    }
    
    override fun filter() = Filter { next ->
        { request ->
            when (val result = authenticate(request)) {
                is AuthResult.Success -> next(request.header("X-Auth-User", result.user.id))
                is AuthResult.Failure -> Response(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"OnMind-XDB\"")
                    .body(result.reason)
            }
        }
    }
}

class AutheliaProvider(private val autheliaUrl: String) : AuthProvider {
    override fun authenticate(request: Request): AuthResult {
        val remoteUser = request.header("Remote-User") ?: return AuthResult.Failure("No Remote-User header")
        val remoteEmail = request.header("Remote-Email") ?: remoteUser
        val remoteName = request.header("Remote-Name") ?: remoteUser
        val remoteGroups = request.header("Remote-Groups")?.split(",") ?: emptyList()
        
        return AuthResult.Success(
            AuthUser(id = remoteUser, email = remoteEmail, name = remoteName, roles = remoteGroups)
        )
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
}

class CognitoProvider(
    private val region: String,
    private val userPoolId: String,
    private val clientId: String
) : AuthProvider {
    override fun authenticate(request: Request): AuthResult {
        val token = request.header("Authorization")?.removePrefix("Bearer ")
            ?: return AuthResult.Failure("No Authorization header")
        
        return try {
            val claims = validateCognitoToken(token)
            AuthResult.Success(
                AuthUser(
                    id = claims["sub"] as String,
                    email = claims["email"] as String,
                    name = claims["name"] as? String ?: claims["email"] as String,
                    roles = (claims["cognito:groups"] as? List<*>)?.map { it.toString() } ?: emptyList()
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
    
    private fun validateCognitoToken(token: String): Map<String, Any> {
        // TODO: Implementar validación con AWS Cognito SDK
        throw NotImplementedError("Cognito token validation not implemented")
    }
}
