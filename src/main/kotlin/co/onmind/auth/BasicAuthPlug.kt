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
        val header = request.header("Authorization")
            ?: return AuthResult.Failure("No Authorization header")

        // Reject any scheme other than Basic (e.g. a stray "Bearer ...") with a 401
        // instead of letting Base64 decoding blow up into a 500.
        if (!header.startsWith("Basic ", ignoreCase = true)) {
            return AuthResult.Failure("Unsupported authorization scheme")
        }

        val decoded = try {
            String(java.util.Base64.getDecoder().decode(header.substring(6).trim()))
        } catch (ex: IllegalArgumentException) {
            return AuthResult.Failure("Malformed Authorization header")
        }

        val separator = decoded.indexOf(':')
        if (separator < 0) {
            return AuthResult.Failure("Malformed credentials (expected user:password)")
        }

        val reqUser = decoded.substring(0, separator)
        val reqPass = decoded.substring(separator + 1)

        // Constant-time comparison to avoid leaking length/prefix information.
        val ok = constantTimeEquals(reqUser, user) and constantTimeEquals(reqPass, pass)

        return if (ok) {
            AuthResult.Success(AuthUser(id = user, email = "$user@local", name = user.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                getDefault()
            ) else it.toString() }, roles = listOf("ADMIN")))
        } else {
            AuthResult.Failure("Invalid credentials")
        }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        java.security.MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
    
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
