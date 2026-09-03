package co.onmind.auth

/**
 * Validates a real access token issued by a running OnMind-UID server.
 * Reads UID_ACCESS_TOKEN / UID_JWT_SECRET / UID_ISSUER from env (set by smoke script).
 * Run via the shared classpath used for JwtValidatorTest.
 */
object JwtValidatorInteropTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val secret = System.getenv("UID_JWT_SECRET")
        val token = System.getenv("UID_ACCESS_TOKEN")
        val issuer = System.getenv("UID_ISSUER")
        if (secret.isNullOrEmpty() || token.isNullOrEmpty()) {
            println("SKIP: UID_* env not set")
            return
        }
        val claims = JwtValidator.verify(token, secret, issuer = issuer, audience = "web-client-1")
            ?: error("UID token rejected by validator")
        println("UID token validated: username=${claims["username"]} groups=${claims["cognito:groups"]}")
        check(claims["username"] == "alice")
        check((claims["cognito:groups"] as? List<*>)?.size == 2)
        println("Interop OK")
    }
}
