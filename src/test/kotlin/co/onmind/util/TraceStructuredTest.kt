package co.onmind.util

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.Properties

/**
 * Verifies the structured logging in OnMind-XDB by writing to a temporary log
 * file and inspecting its content (Trace buffers + flushes on shutdown).
 * Mutates global state (CoherenceConfig, Trace), reset afterwards.
 */
class TraceStructuredTest {

    @Test
    fun basicStructuredLogging() {
        val log = captureLog(level = 2) {
            Trace.infoWith(
                "Test message",
                "string_field" to "hello",
                "number_field" to 42,
                "boolean_field" to true
            )
        }
        assertTrue(log.contains("Test message"), "Should contain message; log=$log")
        assertTrue(log.contains("string_field=\"hello\""), "Should contain string field; log=$log")
        assertTrue(log.contains("number_field=42"), "Should contain number field; log=$log")
        assertTrue(log.contains("boolean_field=true"), "Should contain boolean field; log=$log")
    }

    @Test
    fun httpRequestLogging() {
        val log = captureLog(level = 1) {
            Trace.logHttpRequest("POST", "/abc", 200, 150)
        }
        assertTrue(log.contains("HTTP request"), "Should contain HTTP request; log=$log")
        assertTrue(log.contains("method=\"POST\""), "Should contain method; log=$log")
        assertTrue(log.contains("path=\"/abc\""), "Should contain path; log=$log")
        assertTrue(log.contains("status=200"), "Should contain status; log=$log")
        assertTrue(log.contains("duration_ms=150"), "Should contain duration; log=$log")
    }

    @Test
    fun databaseOperationLogging() {
        val log = captureLog(level = 2) {
            Trace.logDatabaseOperation("insert", "any", true, mapOf(
                "table" to "xyany",
                "records" to 5
            ))
        }
        assertTrue(log.contains("Database operation completed"), "Should contain operation message; log=$log")
        assertTrue(log.contains("operation=\"insert\""), "Should contain operation; log=$log")
        assertTrue(log.contains("entity=\"any\""), "Should contain entity; log=$log")
        assertTrue(log.contains("success=true"), "Should contain success; log=$log")
        assertTrue(log.contains("table=\"xyany\""), "Should contain table; log=$log")
        assertTrue(log.contains("records=5"), "Should contain records; log=$log")
    }

    @Test
    fun coherenceCheckLogging() {
        val log = captureLog(level = 2) {
            Trace.logCoherenceCheck("any", 150, 150, true)
        }
        assertTrue(log.contains("Coherence check OK"), "Should contain coherence message; log=$log")
        assertTrue(log.contains("entity=\"any\""), "Should contain entity; log=$log")
        assertTrue(log.contains("memory_count=150"), "Should contain memory count; log=$log")
        assertTrue(log.contains("disk_count=150"), "Should contain disk count; log=$log")
        assertTrue(log.contains("coherent=true"), "Should contain coherent status; log=$log")
    }

    @Test
    fun errorLogging() {
        val log = captureLog(level = 1) {
            val exception = RuntimeException("Test error")
            Trace.logError("Operation failed", exception, mapOf(
                "operation" to "test",
                "retry_count" to 3
            ))
        }
        assertTrue(log.contains("Operation failed"), "Should contain error message; log=$log")
        assertTrue(log.contains("operation=\"test\""), "Should contain operation; log=$log")
        assertTrue(log.contains("retry_count=3"), "Should contain retry count; log=$log")
        assertTrue(log.contains("exception=\"RuntimeException\""), "Should contain exception type; log=$log")
        assertTrue(log.contains("error_message=\"Test error\""), "Should contain error message; log=$log")
    }

    @Test
    fun zeroOverheadWhenDisabled() {
        val log = captureLog(level = 0) {
            // Estos no deberían producir salida
            Trace.infoWith("This should not appear", "key" to "value")
            Trace.logHttpRequest("GET", "/test", 200)
            Trace.logCoherenceCheck("any", 100, 100, true)
        }
        assertTrue(log.isBlank(), "Should have no output when disabled; log=$log")
    }

    /** Runs [block] with logging at [level] into a temp file and returns the file content. */
    private fun captureLog(level: Int, block: () -> Unit): String {
        val file = Files.createTempFile("xdb-trace", ".log").toFile()
        file.delete()
        CoherenceConfig.init(Properties().apply {
            setProperty("app.logger", level.toString())
            setProperty("db.check", if (level > 0) "+" else "-")
        })
        Trace.init(file.absolutePath, level)
        return try {
            block()
            Trace.shutdown()
            if (file.exists()) file.readText() else ""
        } finally {
            file.delete()
        }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun resetGlobalState() {
            // Restore zero-overhead defaults so other test classes are unaffected.
            CoherenceConfig.init(Properties().apply {
                setProperty("app.logger", "0")
                setProperty("db.check", "-")
            })
            Trace.init("", 0)
        }
    }
}
