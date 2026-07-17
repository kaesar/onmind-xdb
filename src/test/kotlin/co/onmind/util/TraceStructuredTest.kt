package co.onmind.util

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Properties

/**
 * Test simple para verificar el logging estructurado en OnMind-XDB
 * No requiere frameworks externos, solo assertions básicas
 */
object TraceStructuredTest {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== OnMind-XDB Structured Logging Test ===")
        
        try {
            testBasicStructuredLogging()
            testHttpRequestLogging()
            testDatabaseOperationLogging()
            testCoherenceCheckLogging()
            testErrorLogging()
            testZeroOverheadWhenDisabled()
            
            println("All tests passed!")
        } catch (e: Exception) {
            println("Test failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun testBasicStructuredLogging() {
        println("\n--- Test: Basic Structured Logging ---")
        
        val output = captureOutput {
            setupLogging(2) // Debug completo
            
            Trace.infoWith("Test message", 
                "string_field" to "hello",
                "number_field" to 42,
                "boolean_field" to true
            )
        }
        
        println("Output: $output")
        assert(output.contains("Test message")) { "Should contain message" }
        assert(output.contains("string_field=\"hello\"")) { "Should contain string field" }
        assert(output.contains("number_field=42")) { "Should contain number field" }
        assert(output.contains("boolean_field=true")) { "Should contain boolean field" }
        
        println("Basic structured logging works")
    }
    
    private fun testHttpRequestLogging() {
        println("\n--- Test: HTTP Request Logging ---")
        
        val output = captureOutput {
            setupLogging(1) // Solo peticiones
            
            Trace.logHttpRequest("POST", "/abc", 200, 150)
        }
        
        println("Output: $output")
        assert(output.contains("HTTP request")) { "Should contain HTTP request" }
        assert(output.contains("method=\"POST\"")) { "Should contain method" }
        assert(output.contains("path=\"/abc\"")) { "Should contain path" }
        assert(output.contains("status=200")) { "Should contain status" }
        assert(output.contains("duration_ms=150")) { "Should contain duration" }
        
        println("HTTP request logging works")
    }
    
    private fun testDatabaseOperationLogging() {
        println("\n--- Test: Database Operation Logging ---")
        
        val output = captureOutput {
            setupLogging(2) // Debug completo
            
            Trace.logDatabaseOperation("insert", "any", true, mapOf(
                "table" to "xyany",
                "records" to 5
            ))
        }
        
        println("Output: $output")
        assert(output.contains("Database operation completed")) { "Should contain operation message" }
        assert(output.contains("operation=\"insert\"")) { "Should contain operation" }
        assert(output.contains("entity=\"any\"")) { "Should contain entity" }
        assert(output.contains("success=true")) { "Should contain success" }
        assert(output.contains("table=\"xyany\"")) { "Should contain table" }
        assert(output.contains("records=5")) { "Should contain records" }
        
        println("Database operation logging works")
    }
    
    private fun testCoherenceCheckLogging() {
        println("\n--- Test: Coherence Check Logging ---")
        
        val output = captureOutput {
            setupLogging(2) // Debug completo
            
            Trace.logCoherenceCheck("any", 150, 150, true)
        }
        
        println("Output: $output")
        assert(output.contains("Coherence check OK")) { "Should contain coherence message" }
        assert(output.contains("entity=\"any\"")) { "Should contain entity" }
        assert(output.contains("memory_count=150")) { "Should contain memory count" }
        assert(output.contains("disk_count=150")) { "Should contain disk count" }
        assert(output.contains("coherent=true")) { "Should contain coherent status" }
        
        println("Coherence check logging works")
    }
    
    private fun testErrorLogging() {
        println("\n--- Test: Error Logging with Context ---")
        
        val output = captureOutput {
            setupLogging(1) // Peticiones y errores
            
            val exception = RuntimeException("Test error")
            Trace.logError("Operation failed", exception, mapOf(
                "operation" to "test",
                "retry_count" to 3
            ))
        }
        
        println("Output: $output")
        assert(output.contains("Operation failed")) { "Should contain error message" }
        assert(output.contains("operation=\"test\"")) { "Should contain operation" }
        assert(output.contains("retry_count=3")) { "Should contain retry count" }
        assert(output.contains("exception=\"RuntimeException\"")) { "Should contain exception type" }
        assert(output.contains("error_message=\"Test error\"")) { "Should contain error message" }
        
        println("Error logging with context works")
    }
    
    private fun testZeroOverheadWhenDisabled() {
        println("\n--- Test: Zero Overhead When Disabled ---")
        
        val output = captureOutput {
            setupLogging(0) // Desactivado
            
            // Estos no deberían producir salida
            Trace.infoWith("This should not appear", "key" to "value")
            Trace.logHttpRequest("GET", "/test", 200)
            Trace.logCoherenceCheck("any", 100, 100, true)
        }
        
        println("Output: '$output'")
        assert(output.isEmpty() || output.isBlank()) { "Should have no output when disabled" }
        
        println("Zero overhead when disabled works")
    }
    
    private fun setupLogging(level: Int) {
        // Configurar CoherenceConfig
        val config = Properties()
        config.setProperty("app.logger", level.toString())
        config.setProperty("db.check", if (level > 0) "+" else "-")
        CoherenceConfig.init(config)
        
        // Inicializar Trace sin archivo (solo consola)
        Trace.init("", 0) // Forzar modo consola
    }
    
    private fun captureOutput(block: () -> Unit): String {
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        
        return try {
            System.setOut(PrintStream(outputStream))
            block()
            outputStream.toString().trim()
        } finally {
            System.setOut(originalOut)
        }
    }
}