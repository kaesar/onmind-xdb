package co.onmind

import co.onmind.api.WithFilter
import co.onmind.auth.BasicAuthPlug
import co.onmind.auth.OIDCPlug
import co.onmind.trait.AuthResult
import co.onmind.util.ConfigSecrets
import co.onmind.util.Rote
import co.onmind.util.Sql
import onmindxdb
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Base64

/**
 * Regression checks for the pre-release robustness/security hardening.
 *
 * Standard JUnit 5 tests, executed by `./gradlew test`.
 */
class SecurityChecksTest {

    private fun basic(user: String, pass: String): String =
        "Basic " + Base64.getEncoder().encodeToString("$user:$pass".toByteArray())

    private fun props(vararg pairs: Pair<String, String>) = java.util.Properties().apply {
        pairs.forEach { (k, v) -> setProperty(k, v) }
    }

    @Test
    fun `SQL values are quoted, never interpolated`() {
        assertEquals("'O''Hara'", Sql.literal("O'Hara"))
        assertEquals("'x'' OR ''1''=''1'", Sql.literal("x' OR '1'='1"))
        assertEquals("''", Sql.literal(null))
        assertEquals("'42'", Sql.literal(42))
    }

    @Test
    fun `BasicAuthPlug rejects malformed input with a failure, not a crash`() {
        val plug = BasicAuthPlug("admin", "admin")
        assertTrue(plug.authenticate(Request(Method.GET, "/")) is AuthResult.Failure, "no header")
        assertTrue(
            plug.authenticate(Request(Method.GET, "/").header("Authorization", "Bearer abc")) is AuthResult.Failure,
            "Bearer scheme"
        )
        assertTrue(
            plug.authenticate(Request(Method.GET, "/").header("Authorization", "Basic !!!")) is AuthResult.Failure,
            "broken base64"
        )
        val noColon = "Basic " + Base64.getEncoder().encodeToString("admin".toByteArray())
        assertTrue(
            plug.authenticate(Request(Method.GET, "/").header("Authorization", noColon)) is AuthResult.Failure,
            "no colon"
        )
        assertTrue(
            plug.authenticate(Request(Method.GET, "/").header("Authorization", basic("admin", "admin"))) is AuthResult.Success,
            "valid credentials"
        )
        assertTrue(
            plug.authenticate(Request(Method.GET, "/").header("Authorization", basic("admin", "nope"))) is AuthResult.Failure,
            "wrong credentials"
        )
    }

    @Test
    fun `OIDC proxy headers are ignored unless explicitly trusted`() {
        val direct = OIDCPlug(clientId = "test")
        assertTrue(
            direct.authenticate(Request(Method.GET, "/").header("X-Auth-User", "root")) is AuthResult.Failure,
            "header ignored by default"
        )
        val delegated = OIDCPlug(clientId = "test", trustProxyHeaders = true)
        assertTrue(
            delegated.authenticate(Request(Method.GET, "/").header("X-Auth-User", "root")) is AuthResult.Success,
            "header honored when trusted"
        )
    }

    @Test
    fun `config secrets are masked on read and preserved on save`() {
        val ini = """
            app.mode = production
            auth.basic.pass = YWRtaW4=
            s3.secret_key =
            db.password = secret#1
            auth.otp.session_key = k3y
        """.trimIndent()

        val masked = ConfigSecrets.mask(ini)
        assertTrue(masked.contains("auth.basic.pass = ${ConfigSecrets.PLACEHOLDER}"), "basic pass masked")
        assertTrue(masked.contains("db.password = ${ConfigSecrets.PLACEHOLDER}"), "db.password masked")
        assertFalse(masked.contains("YWRtaW4=") || masked.contains("secret#1"), "secret value not leaked")
        assertTrue(
            masked.contains("s3.secret_key =") && !masked.contains("s3.secret_key = ${ConfigSecrets.PLACEHOLDER}"),
            "empty secret stays empty"
        )
        assertTrue(masked.contains("app.mode = production"), "non-secret untouched")

        val saved = ConfigSecrets.restore(masked, ini)
        assertTrue(saved.contains("auth.basic.pass = YWRtaW4="), "round-trip restores basic pass")
        assertTrue(saved.contains("db.password = secret#1"), "round-trip restores db.password")

        val edited = masked.replace("app.mode = production", "app.mode = development")
        val saved2 = ConfigSecrets.restore(edited, ini)
        assertTrue(saved2.contains("auth.basic.pass = YWRtaW4="), "edit keeps secrets")
        assertTrue(saved2.contains("app.mode = development"), "edit applied")

        val changed = masked.replace("auth.basic.pass = ${ConfigSecrets.PLACEHOLDER}", "auth.basic.pass = newpass")
        assertTrue(ConfigSecrets.restore(changed, ini).contains("auth.basic.pass = newpass"), "typed new secret wins")

        val noStored = ConfigSecrets.restore(masked, "app.mode = production")
        assertTrue(
            noStored.contains("auth.basic.pass =") && !noStored.contains(ConfigSecrets.PLACEHOLDER),
            "placeholder without stored value never persists stars"
        )
    }

    @Test
    fun `int properties fall back to a default instead of crashing the boot`() {
        assertEquals(9990, Rote.intProp(java.util.Properties(), "dai.port", 9990, 1..65535), "missing key")
        assertEquals(9990, Rote.intProp(props("dai.port" to "9990"), "dai.port", 9990, 1..65535), "valid value")
        assertEquals(9990, Rote.intProp(props("dai.port" to "9990  # HTTP"), "dai.port", 9990, 1..65535), "inline # comment")
        assertEquals(9990, Rote.intProp(props("dai.port" to " 9990 "), "dai.port", 9990, 1..65535), "spaces trimmed")
        assertEquals(9990, Rote.intProp(props("dai.port" to "nueve"), "dai.port", 9990, 1..65535), "malformed")
        assertEquals(9990, Rote.intProp(props("dai.port" to "99999"), "dai.port", 9990, 1..65535), "out of range")
        assertEquals(9991, Rote.intProp(props("grpc.port" to "0"), "grpc.port", 9991, 1..65535), "zero")
        assertEquals(25, Rote.intProp(props("db.max_pool_size" to "25"), "db.max_pool_size", 10), "no range accepts any int")
        assertEquals(1200, Rote.intProp(props("db.query_limit" to "1200  # rows"), "db.query_limit", 1200, 1..1_000_000), "query_limit comment")
        assertEquals(1200, Rote.intProp(props("db.query_limit" to "mil"), "db.query_limit", 1200, 1..1_000_000), "query_limit malformed")
        assertEquals(120, Rote.intProp(props("mcp.llm.timeout" to "x"), "mcp.llm.timeout", 120, 1..86400), "timeout malformed")
        assertEquals(300, Rote.intProp(props("mcp.llm.timeout" to "300"), "mcp.llm.timeout", 120, 1..86400), "timeout valid")
    }

    @Test
    fun `boolean flags use the eXpress +- convention and stay tolerant`() {
        // Canonical form: + = true, - = false.
        assertTrue(Rote.boolProp(props("db.export" to "+"), "db.export", false), "+ is true")
        assertFalse(Rote.boolProp(props("db.export" to "-"), "db.export", false), "- is false")
        // Tolerant aliases (so older/other spellings keep working).
        assertTrue(Rote.boolProp(props("db.export" to "true"), "db.export", false), "true alias")
        assertFalse(Rote.boolProp(props("db.export" to "false"), "db.export", false), "false alias")
        assertTrue(Rote.boolProp(props("db.export" to "ON"), "db.export", false), "case-insensitive alias")
        // Inline comment and whitespace.
        assertTrue(Rote.boolProp(props("db.export" to " +  # export"), "db.export", false), "inline # comment")
        assertFalse(Rote.boolProp(props("db.export" to " - "), "db.export", false), "spaces trimmed")
        // Missing / unknown value falls back to the provided default.
        assertTrue(Rote.boolProp(java.util.Properties(), "auth.enabled", true), "missing -> default")
        assertFalse(Rote.boolProp(props("x" to "maybe"), "x", false), "unknown -> default")
        // `auth.enabled` must stay true for both the canonical and the legacy spelling.
        assertTrue(Rote.boolProp(props("auth.enabled" to "+"), "auth.enabled", true), "auth + is true")
        assertTrue(Rote.boolProp(props("auth.enabled" to "true"), "auth.enabled", true), "auth true is true")
        assertFalse(Rote.boolProp(props("auth.enabled" to "-"), "auth.enabled", true), "auth - is false")
        assertFalse(Rote.boolProp(props("auth.enabled" to "false"), "auth.enabled", true), "auth false is false")
    }

    @Test
    fun `onmind ini ignores inline comments but keeps '#' inside values`() {
        val iniFile = java.io.File.createTempFile("xdb-ini", ".ini")
        try {
            iniFile.writeText(
                """
                # leading comment
                db.export = -      # + enables export
                app.ui = +         # dashboard
                auth.basic.pass = pa#ss
                db.password = "a # b"
                s3.endpoint = http://rustfs:9000/path#frag
                db.host = localhost   ; not a comment in Properties
                """.trimIndent()
            )
            val cfg = Rote.getConfig(iniFile.absolutePath)
            assertEquals("-", cfg.getProperty("db.export"), "inline comment dropped (db.export)")
            assertEquals("+", cfg.getProperty("app.ui"), "inline comment dropped (app.ui)")
            assertEquals("pa#ss", cfg.getProperty("auth.basic.pass"), "space-less '#' kept in secret")
            assertTrue(cfg.getProperty("db.password")?.contains("#") == true, "quoted value with '#' kept")
            assertEquals("http://rustfs:9000/path#frag", cfg.getProperty("s3.endpoint"), "URL fragment '#' kept")
            assertNull(cfg.getProperty("# leading comment"), "leading comment ignored")
            assertTrue(
                cfg.getProperty("db.host")?.let { it.startsWith("localhost") && it.contains(";") } == true,
                "';' is not a comment char"
            )
        } finally {
            iniFile.delete()
        }
    }

    @Test
    fun `WITH filter accepts the documented grammar and rejects SQL smuggling`() {
        // Accepted: the express usage stays usable.
        val accepted = listOf(
            "any03 LIKE 'lab%'",
            "any02 = 'producto-1'",
            "any03 = 'demo'",
            "any02 = 'x' AND any03 LIKE 'lab%'",
            "any02 = 'x' OR any03 = 'y'",
            "any04 > 10",
            "any04 <= 3.5",
            "any05 != 'a'",
            "any06 <> 'b'",
            "any07 = NULL",
            "any08 NOT LIKE 'tmp%'",
            "any09 LIKE 'lab'",
            "any03 LIKE '%lab%'",
            "any03 NOT LIKE '%lab%'",
            "any03 LIKE '%lab'",
            "any03 LIKE 'la%b'",
            "any03 LIKE '%a%b%'",
            "any03 LIKE '%%'",
            "any02 = \"x\"",
            "any02 = any03",
            "any02 = 'x'' OR ''1''=''1'"   // a quoted literal, not SQL
        )
        accepted.forEach { assertNull(WithFilter.validate(it), "should accept: $it") }

        // Rejected: no way to smuggle SQL through `with`.
        val rejected = listOf(
            "1=1",
            "any02 = 'x' OR 1=1",
            "any02 = 'x' UNION SELECT key01 FROM xykey",
            "any02 = 'x'; DROP TABLE xyany",
            "any02 = 'x' -- comment",
            "any02 = 'x' /* comment */",
            "(any02 = 'x')",
            "any02 = 'x') OR (any03 = 'y'",
            "any03 LIKE 'lab",
            "any02 =",
            "= 'x'",
            "any02 = 'x' AND",
            "any02 = 'x' XOR any03 = 'y'",
            "SUBSTR(any02, 1, 1) = 'a'",
            "any02 = 'x' SELECT 1",
            "any02 = UNION",
            "any02 = SELECT",
            "any02 = FROM",
            "SELECT = 'x'"
        )
        rejected.forEach { assertTrue(WithFilter.validate(it) != null, "should reject: $it") }
    }

    @Test
    fun `export file reservation is atomic (no TOCTOU)`() {
        val base = java.nio.file.Files.createTempDirectory("xdb-export-test").toFile()
        val previousDbFile = onmindxdb.dbfile
        onmindxdb.dbfile = java.io.File(base, "xy/xybox.xdb").absolutePath
        try {
            val rdb = co.onmind.db.RDB()
            val a = rdb.resolveExportFile()
            val b = rdb.resolveExportFile()
            assertNotEquals(a.absolutePath, b.absolutePath, "distinct paths")
            assertTrue(a.exists() && b.exists(), "files reserved up front")
            assertTrue(b.name.contains("_1"), "second reservation gets _1 suffix")

            val paths = java.util.Collections.synchronizedSet(mutableSetOf<String>())
            val threads = (1..8).map { Thread { paths.add(rdb.resolveExportFile().absolutePath) } }
            threads.forEach { it.start() }
            threads.forEach { it.join() }
            assertEquals(8, paths.size, "8 concurrent reservations are unique")
        } finally {
            onmindxdb.dbfile = previousDbFile
            base.deleteRecursively()
        }
    }
}
