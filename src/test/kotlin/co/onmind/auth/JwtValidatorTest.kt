package co.onmind.auth

import co.onmind.util.JsonMapper
import org.junit.jupiter.api.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

/** Verifies the HS256 JWT validator used for OnMind-UID resource-server auth. */
class JwtValidatorTest {

    private val secret = "test-shared-secret"

    @Test
    fun validToken() {
        val claims = mapOf(
            "iss" to "http://localhost:8080",
            "sub" to "alice",
            "username" to "alice",
            "aud" to "xdb-client",
            "token_use" to "access",
            "email" to "alice@example.com",
            "cognito:groups" to listOf("admins", "readers"),
            "exp" to (Instant.now().plusSeconds(3600).epochSecond)
        )
        val token = sign(claims)
        val result = JwtValidator.verify(token, secret, issuer = "http://localhost:8080", audience = "xdb-client")
        check(result != null) { "should validate" }
        check(result["username"] == "alice")
        check((result["cognito:groups"] as? List<*>)?.size == 2)
    }

    @Test
    fun badSignatureRejected() {
        val claims = mapOf("iss" to "http://localhost:8080", "exp" to Instant.now().plusSeconds(3600).epochSecond)
        val token = sign(claims, overrideSecret = "other-secret")
        val result = JwtValidator.verify(token, secret)
        check(result == null) { "should reject bad signature" }
    }

    @Test
    fun expiredTokenRejected() {
        val claims = mapOf("exp" to Instant.now().minusSeconds(60).epochSecond)
        val token = sign(claims)
        val result = JwtValidator.verify(token, secret)
        check(result == null) { "should reject expired token" }
    }

    @Test
    fun wrongIssuerRejected() {
        val claims = mapOf("iss" to "http://evil.example", "exp" to Instant.now().plusSeconds(3600).epochSecond)
        val token = sign(claims)
        val result = JwtValidator.verify(token, secret, issuer = "http://localhost:8080")
        check(result == null) { "should reject wrong issuer" }
    }

    private fun sign(claims: Map<String, Any>, overrideSecret: String = secret): String {
        val header = base64Url("""{"alg":"HS256","typ":"JWT"}""")
        val payload = base64Url(JsonMapper.instance.writeValueAsString(claims))
        val signingInput = "$header.$payload"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(overrideSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val sig = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(signingInput.toByteArray(StandardCharsets.UTF_8)))
        return "$signingInput.$sig"
    }

    private fun base64Url(s: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray(StandardCharsets.UTF_8))
}
