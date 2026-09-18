package co.onmind.auth

import co.onmind.util.JsonMapper
import co.onmind.util.Rote
import com.sun.net.httpserver.HttpServer
import java.math.BigInteger
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import java.util.Properties

/**
 * Verifies RS256/JWKS validation (OIDCPlug real-JWT path) and dai.cors parsing.
 * eXpress default must keep working: no JWKS/secret -> legacy decode-only.
 */
object JwksValidatorTest {

    private val json = JsonMapper.instance
    private const val KID = "test-key-1"

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== JwksValidator Test ===")
        try {
            testRs256Roundtrip()
            testTamperedRejected()
            testExpiredRejected()
            testWrongIssuerRejected()
            testAudienceCheck()
            testFetchFromUrl()
            testKeycloakDerivation()
            testCorsParsing()
            testExpressLegacyFallback()
            println("All JwksValidator tests passed!")
        } catch (e: Exception) {
            println("\nJwksValidator test failed: ${e.message}")
            e.printStackTrace()
            kotlin.system.exitProcess(1)
        }
    }

    private data class Fixture(
        val jwksJson: String,
        val sign: (claims: Map<String, Any>) -> String
    )

    private fun fixture(): Fixture {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val kp = kpg.generateKeyPair()
        val pub = kp.public as RSAPublicKey
        val n = base64UrlUnsigned(pub.modulus)
        val e = base64UrlUnsigned(pub.publicExponent)
        val jwksJson = """{"keys":[{"kty":"RSA","kid":"$KID","use":"sig","alg":"RS256","n":"$n","e":"$e"}]}"""
        return Fixture(jwksJson) { claims -> signRs256(kp.private, claims, KID) }
    }

    private fun baseClaims(expOffsetSeconds: Long = 3600): Map<String, Any> = mapOf(
        "iss" to "https://auth.example.com/realms/master",
        "sub" to "alice",
        "aud" to "onmind-xdb",
        "email" to "alice@example.com",
        "exp" to Instant.now().plusSeconds(expOffsetSeconds).epochSecond
    )

    private fun testRs256Roundtrip() {
        val f = fixture()
        val token = f.sign(baseClaims())
        val claims = JwksValidator.verifyWithJwksJson(
            token, f.jwksJson,
            issuer = "https://auth.example.com/realms/master",
            audience = "onmind-xdb"
        )
        check(claims != null) { "valid RS256 token should verify" }
        check(claims!!["sub"] == "alice")
        println("ok: RS256 roundtrip verified sub=${claims["sub"]}")
    }

    private fun testTamperedRejected() {
        val f = fixture()
        val token = f.sign(baseClaims())
        val parts = token.split(".")
        val tamperedPayload = base64Url(
            json.writeValueAsString(baseClaims() + ("sub" to "mallory"))
        )
        val tampered = "${parts[0]}.$tamperedPayload.${parts[2]}"
        val claims = JwksValidator.verifyWithJwksJson(tampered, f.jwksJson)
        check(claims == null) { "tampered payload must be rejected" }
        println("ok: tampered payload rejected")
    }

    private fun testExpiredRejected() {
        val f = fixture()
        val token = f.sign(baseClaims(expOffsetSeconds = -60))
        check(JwksValidator.verifyWithJwksJson(token, f.jwksJson) == null) { "expired must be rejected" }
        println("ok: expired token rejected")
    }

    private fun testWrongIssuerRejected() {
        val f = fixture()
        val token = f.sign(baseClaims())
        val bad = JwksValidator.verifyWithJwksJson(
            token, f.jwksJson, issuer = "https://evil.example/realms/master"
        )
        check(bad == null) { "wrong issuer must be rejected" }
        val none = JwksValidator.verifyWithJwksJson(token, f.jwksJson)
        check(none != null) { "no issuer constraint should verify" }
        println("ok: issuer check enforced only when configured")
    }

    private fun testAudienceCheck() {
        val f = fixture()
        val token = f.sign(baseClaims())
        check(JwksValidator.verifyWithJwksJson(token, f.jwksJson, audience = "other") == null) {
            "wrong audience must be rejected"
        }
        check(JwksValidator.verifyWithJwksJson(token, f.jwksJson, audience = "onmind-xdb") != null) {
            "matching audience should verify"
        }
        println("ok: audience check enforced only when configured")
    }

    private fun testFetchFromUrl() {
        val f = fixture()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/certs") { exchange ->
            val body = f.jwksJson.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            JwksValidator.clearCache()
            val url = "http://127.0.0.1:${server.address.port}/certs"
            val token = f.sign(baseClaims())
            val claims = JwksValidator.verify(token, url)
            check(claims != null) { "JWKS fetched over HTTP should verify" }
            check(claims!!["sub"] == "alice")
            println("ok: JWKS fetched from $url and verified")
        } finally {
            server.stop(0)
            JwksValidator.clearCache()
        }
    }

    private fun testKeycloakDerivation() {
        val derived = OIDCPlug.defaultJwksUrl("http://localhost:8080/", "master", "KEYCLOAK")
        check(derived == "http://localhost:8080/realms/master/protocol/openid-connect/certs") {
            "unexpected derivation: $derived"
        }
        check(OIDCPlug.defaultJwksUrl("http://localhost:8080", "master", "ENTRAID") == null) {
            "ENTRAID must require explicit jwks_url"
        }
        check(OIDCPlug.defaultJwksUrl(null, "master", "KEYCLOAK") == null) {
            "missing serverUrl must not derive"
        }
        println("ok: Keycloak JWKS URL derived: $derived")
    }

    private fun testCorsParsing() {
        // eXpress defaults -> AllowAll (null)
        check(Rote.parseCorsOrigins(null) == null) { "null -> AllowAll" }
        check(Rote.parseCorsOrigins("") == null) { "empty -> AllowAll" }
        check(Rote.parseCorsOrigins("*") == null) { "* -> AllowAll" }
        check(Rote.parseCorsOrigins("  *  ") == null) { "padded * -> AllowAll" }
        // Restricted list
        val list = Rote.parseCorsOrigins("https://app.example.com, https://admin.example.com")
        check(list == listOf("https://app.example.com", "https://admin.example.com")) { "got $list" }
        check(Rote.parseCorsOrigins("https://a.example/") == listOf("https://a.example")) { "trailing slash" }
        // Properties wiring with default "*"
        check(Rote.corsOrigins(Properties()) == null) { "missing prop -> AllowAll" }
        val props = Properties().apply { setProperty("dai.cors", "https://a.example,https://b.example") }
        check(Rote.corsOrigins(props) == listOf("https://a.example", "https://b.example")) { "props wiring" }
        println("ok: dai.cors parsing (AllowAll default + comma list)")
    }

    private fun testExpressLegacyFallback() {
        // No JWKS, no secret -> legacy decode-only keeps working (backward compat).
        val plug = OIDCPlug(clientId = "onmind-xdb")
        val payload = base64Url(json.writeValueAsString(baseClaims()))
        val token = "eyJhbGciOiJSUzI1NiJ9.$payload.fake-signature"
        val request = org.http4k.core.Request(org.http4k.core.Method.POST, "/abc")
            .header("Authorization", "Bearer $token")
        val result = plug.authenticate(request)
        check(result is co.onmind.trait.AuthResult.Success) { "legacy fallback must accept, got $result" }
        check((result as co.onmind.trait.AuthResult.Success).user.id == "alice")
        println("ok: eXpress legacy fallback (no JWKS/secret) still decodes")
    }

    private fun signRs256(privateKey: java.security.PrivateKey, claims: Map<String, Any>, kid: String?): String {
        val header = if (kid != null) """{"alg":"RS256","typ":"JWT","kid":"$kid"}"""
        else """{"alg":"RS256","typ":"JWT"}"""
        val h = base64Url(header)
        val p = base64Url(json.writeValueAsString(claims))
        val signingInput = "$h.$p".toByteArray(StandardCharsets.US_ASCII)
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initSign(privateKey)
        sig.update(signingInput)
        return "$h.$p." + Base64.getUrlEncoder().withoutPadding().encodeToString(sig.sign())
    }

    private fun base64Url(s: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray(StandardCharsets.UTF_8))

    private fun base64UrlUnsigned(n: BigInteger): String {
        val bytes = n.toByteArray().let { if (it.size > 1 && it[0] == 0.toByte()) it.drop(1).toByteArray() else it }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
