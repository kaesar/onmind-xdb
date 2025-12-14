package co.onmind.auth

import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class AutheliaPlug(private val autheliaUrl: String) : AuthProvider {
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
