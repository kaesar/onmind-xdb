package co.onmind.auth

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * Validates a real access token issued by a running OnMind-UID server.
 * Reads UID_ACCESS_TOKEN / UID_JWT_SECRET / UID_ISSUER from env (set by smoke script).
 * Skipped automatically when those variables are not present.
 */
class JwtValidatorInteropTest {

    @Test
    fun validatesRealUidToken() {
        val secret = System.getenv("UID_JWT_SECRET")
        val token = System.getenv("UID_ACCESS_TOKEN")
        val issuer = System.getenv("UID_ISSUER")
        assumeTrue(!secret.isNullOrEmpty() && !token.isNullOrEmpty()) {
            "UID_* env not set; skipping UID interop test"
        }
        val claims = JwtValidator.verify(token!!, secret!!, issuer = issuer, audience = "web-client-1")
            ?: error("UID token rejected by validator")
        check(claims["username"] == "alice")
        check((claims["cognito:groups"] as? List<*>)?.size == 2)
    }
}
