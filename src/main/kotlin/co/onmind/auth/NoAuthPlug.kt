package co.onmind.auth

import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import org.http4k.core.Filter
import org.http4k.core.Request

class NoAuthPlug : AuthProvider {
    override fun authenticate(request: Request) = AuthResult.Success(
        AuthUser(id = "admin", email = "admin@local", name = "Admin", roles = listOf("ADMIN"))
    )
    
    override fun filter() = Filter { next -> { request -> next(request) } }
}
