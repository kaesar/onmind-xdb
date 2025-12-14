package co.onmind.trait

import org.http4k.core.Filter
import org.http4k.core.Request

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
