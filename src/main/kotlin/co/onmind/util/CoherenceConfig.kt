package co.onmind.util

import java.util.Properties

/**
 * Configuración optimizada para coherencia y logging en OnMind-XDB
 * Garantiza cero overhead cuando está desactivado
 */
object CoherenceConfig {
    var logLevel: Int = 0           // 0=desactivado, 1=peticiones, 2=debug completo
    var checkEnabled: Boolean = false
    
    fun init(config: Properties) {
        // Parse app.logger (numérico)
        val loggerValue = config.getProperty("app.logger", "0")
        logLevel = when {
            loggerValue.isEmpty() -> 0
            loggerValue == "-" -> 0
            loggerValue == "+" -> 1  // Compatibilidad con formato anterior
            else -> loggerValue.toIntOrNull() ?: 0
        }
        
        // Parse db.check (símbolo)
        val checkValue = config.getProperty("db.check", "-")
        checkEnabled = checkValue == "+"
    }
    
    // Métodos inline para cero overhead cuando está desactivado
    @Suppress("NOTHING_TO_INLINE")
    inline fun shouldLogRequests(): Boolean = logLevel >= 1
    
    @Suppress("NOTHING_TO_INLINE")
    inline fun shouldLogDebug(): Boolean = logLevel >= 2
    
    @Suppress("NOTHING_TO_INLINE")
    inline fun shouldCheckCoherence(): Boolean = checkEnabled
    
    fun getStats(): Map<String, Any> {
        return mapOf(
            "log_level" to logLevel,
            "check_enabled" to checkEnabled,
            "log_requests" to shouldLogRequests(),
            "log_debug" to shouldLogDebug(),
            "check_coherence" to shouldCheckCoherence()
        )
    }
}