package co.onmind.auth

import co.onmind.util.JsonMapper
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

/**
 * Verifies the HS256 JWT validator used for OnMind-UID resource-server auth.
 * Run: cd xdb && JAVA_HOME=<jdk25> ./gradlew -q compileKotlin compileTestKotlin classes && \
 *   kotlin -cp build/classes/kotlin/main:...  (or via your preferred runner)
 */
object JwtValidatorTest {

    private const val SECRET = "test-shared-secret"

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== JwtValidator Test ===")
        try {
            testValidToken()
            testBadSignature()
            testExpiredToken()
            testWrongIssuer()
            println("All JwtValidator tests passed!")
        } catch (e: Exception) {
            println("\nJwtValidator test failed: ${e.message}")
            e.printStackTrace()
            kotlin.system.exitProcess(1)
        }
    }

    private fun testValidToken() {
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
        val result = JwtValidator.verify(token, SECRET, issuer = "http://localhost:8080", audience = "xdb-client")
        check(result != null) { "should validate" }
        check(result!!["username"] == "alice")
        check((result["cognito:groups"] as? List<*>)?.size == 2)
        println("ok: valid token verified, groups=${result["cognito:groups"]}")
    }

    private fun testBadSignature() {
        val claims = mapOf("iss" to "http://localhost:8080", "exp" to Instant.now().plusSeconds(3600).epochSecond)
        val token = sign(claims, overrideSecret = "other-secret")
        val result = JwtValidator.verify(token, SECRET)
        check(result == null) { "should reject bad signature" }
        println("ok: bad signature rejected")
    }

    private fun testExpiredToken() {
        val claims = mapOf("exp" to Instant.now().minusSeconds(60).epochSecond)
        val token = sign(claims)
        val result = JwtValidator.verify(token, SECRET)
        check(result == null) { "should reject expired token" }
        println("ok: expired token rejected")
    }

    private fun testWrongIssuer() {
        val claims = mapOf("iss" to "http://evil.example", "exp" to Instant.now().plusSeconds(3600).epochSecond)
        val token = sign(claims)
        val result = JwtValidator.verify(token, SECRET, issuer = "http://localhost:8080")
        check(result == null) { "should reject wrong issuer" }
        println("ok: wrong issuer rejected")
    }

    private fun sign(claims: Map<String, Any>, overrideSecret: String = SECRET): String {
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
