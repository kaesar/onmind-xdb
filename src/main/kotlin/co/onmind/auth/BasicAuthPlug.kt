package co.onmind.auth

import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import java.util.Locale.getDefault

class BasicAuthPlug(private val user: String, private val pass: String) : AuthProvider {
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
