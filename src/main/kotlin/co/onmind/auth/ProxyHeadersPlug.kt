package co.onmind.auth

import co.onmind.trait.AuthProvider
import co.onmind.trait.AuthResult
import co.onmind.trait.AuthUser
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Forward-auth on trusted proxy headers — the same mechanism as WDB's
 * `auth.oidc.trust_headers = +` (Modo B). Trusts `X-Auth-User` / `X-Proxy-User`
 * (plus email/name/roles) without any token. ONLY safe behind a trusted reverse
 * proxy that strips and rewrites those headers. No Authelia dependency and no
 * Authelia-specific `Remote-*` headers.
 */
class ProxyHeadersPlug : AuthProvider {
    override fun authenticate(request: Request): AuthResult {
        val user = request.header("X-Auth-User") ?: request.header("X-Proxy-User")
            ?: return AuthResult.Failure("No X-Auth-User / X-Proxy-User header")
        val email = request.header("X-Auth-Email") ?: request.header("X-Proxy-Email") ?: user
        val name = request.header("X-Auth-Name") ?: request.header("X-Proxy-Name") ?: user
        val roles = request.header("X-Auth-Roles")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return AuthResult.Success(
            AuthUser(id = user, email = email, name = name, roles = roles)
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