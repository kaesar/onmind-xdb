package co.onmind.util

import java.util.Properties

/** Verifies `os.environ/NAME` resolution with injected env (real env untouched). */
object RoteEnvTest {

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== RoteEnv Test ===")
        try {
            testPlainValuesUntouched()
            testResolvesFromEnv()
            testTrimsReferenceSyntax()
            testMissingVarFailsFast()
            testCorsMissingVarFallsBack()
            testInvalidNameFailsFast()
            testPrefixIsCaseSensitive()
            testMixedProperties()
            println("All RoteEnv tests passed!")
        } catch (e: Exception) {
            println("\nRoteEnv test failed: ${e.message}")
            e.printStackTrace()
            kotlin.system.exitProcess(1)
        }
    }

    private fun testPlainValuesUntouched() {
        val props = Properties().apply {
            setProperty("dai.port", "9990")
            setProperty("app.cors", "*")
            setProperty("auth.jwt.secret", "plain-secret")
        }
        Rote.resolveEnvRefs(props, emptyMap())
        check(props.getProperty("dai.port") == "9990")
        check(props.getProperty("auth.jwt.secret") == "plain-secret")
        println("ok: plain values (eXpress defaults) untouched")
    }

    private fun testResolvesFromEnv() {
        val props = Properties().apply {
            setProperty("auth.jwt.secret", "os.environ/XDB_JWT_SECRET")
            setProperty("dai.port", "os.environ/XDB_PORT")
        }
        val env = mapOf("XDB_JWT_SECRET" to "s3cr3t", "XDB_PORT" to "9991")
        Rote.resolveEnvRefs(props, env)
        check(props.getProperty("auth.jwt.secret") == "s3cr3t") { "got ${props.getProperty("auth.jwt.secret")}" }
        check(props.getProperty("dai.port") == "9991")
        println("ok: os.environ/NAME resolved from environment")
    }

    private fun testTrimsReferenceSyntax() {
        val props = Properties().apply {
            setProperty("auth.jwt.secret", "  os.environ/XDB_JWT_SECRET  ")
        }
        Rote.resolveEnvRefs(props, mapOf("XDB_JWT_SECRET" to "s3cr3t"))
        check(props.getProperty("auth.jwt.secret") == "s3cr3t")
        println("ok: surrounding whitespace around reference is tolerated")
    }

    private fun testMissingVarFailsFast() {
        val props = Properties().apply {
            setProperty("auth.jwt.secret", "os.environ/XDB_JWT_SECRET")
        }
        val error = runCatching { Rote.resolveEnvRefs(props, emptyMap()) }.exceptionOrNull()
        check(error is IllegalStateException) { "expected IllegalStateException, got $error" }
        check(error.message!!.contains("XDB_JWT_SECRET")) { "message must name the variable: ${error.message}" }
        println("ok: missing variable fails fast with clear message")
    }

    private fun testCorsMissingVarFallsBack() {
        // app.cors must never break startup: missing var warns and assumes "*" (AllowAll).
        val props = Properties().apply { setProperty("app.cors", "os.environ/XDB_CORS") }
        Rote.resolveEnvRefs(props, emptyMap())
        check(props.getProperty("app.cors") == "*") { "got ${props.getProperty("app.cors")}" }
        check(Rote.corsOrigins(props) == null) { "fallback must mean AllowAll" }
        // A resolvable reference still wins over the fallback.
        val resolved = Properties().apply { setProperty("app.cors", "os.environ/XDB_CORS") }
        Rote.resolveEnvRefs(resolved, mapOf("XDB_CORS" to "https://app.example.com"))
        check(Rote.corsOrigins(resolved) == listOf("https://app.example.com"))
        println("ok: unresolvable app.cors falls back to '*' (service never breaks)")
    }

    private fun testInvalidNameFailsFast() {
        for (bad in listOf("os.environ/", "os.environ/  ", "os.environ/HAS SPACE", "os.environ/HAS-DASH")) {
            val props = Properties().apply { setProperty("auth.jwt.secret", bad) }
            val error = runCatching { Rote.resolveEnvRefs(props, mapOf("X" to "y")) }.exceptionOrNull()
            check(error is IllegalStateException) { "expected failure for '$bad', got $error" }
        }
        println("ok: empty/invalid variable names rejected")
    }

    private fun testPrefixIsCaseSensitive() {
        val props = Properties().apply {
            setProperty("auth.jwt.secret", "OS.ENVIRON/XDB_JWT_SECRET")
        }
        Rote.resolveEnvRefs(props, mapOf("XDB_JWT_SECRET" to "s3cr3t"))
        check(props.getProperty("auth.jwt.secret") == "OS.ENVIRON/XDB_JWT_SECRET") {
            "prefix must be case-sensitive"
        }
        println("ok: prefix is case-sensitive, other casings untouched")
    }

    private fun testMixedProperties() {
        val props = Properties().apply {
            setProperty("app.cors", "https://app.example.com")
            setProperty("auth.oidc.jwks_url", "os.environ/XDB_JWKS_URL")
            setProperty("auth.jwt.issuer", "http://localhost:8080")
        }
        Rote.resolveEnvRefs(props, mapOf("XDB_JWKS_URL" to "https://auth.example.com/certs"))
        check(props.getProperty("app.cors") == "https://app.example.com")
        check(props.getProperty("auth.oidc.jwks_url") == "https://auth.example.com/certs")
        check(props.getProperty("auth.jwt.issuer") == "http://localhost:8080")
        // Downstream CORS parsing keeps working on resolved values.
        check(Rote.corsOrigins(props) == listOf("https://app.example.com"))
        println("ok: mixed file (literals + references) resolves and composes with CORS parsing")
    }
}
