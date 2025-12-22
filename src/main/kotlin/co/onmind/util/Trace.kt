package co.onmind.util

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Sistema de Trace optimizado para OnMind-XDB con logging estructurado
 * Garantiza cero overhead cuando está desactivado
 * Incluye capacidades de logging estructurado manteniendo compatibilidad total
 */
object Trace {
    private val buffer = mutableListOf<String>()
    private var logFile: File? = null
    private var lastFlush = System.currentTimeMillis()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val flushThreshold = 100
    private val flushInterval = 5000L // 5 seconds
    private var initialized = false
    private var structuredFormat = true // Habilitar formato estructurado por defecto
    
    fun init(filePath: String, level: Int) {
        if (level == 0) {
            initialized = false
            return  // No inicializar nada si está desactivado
        }
        
        try {
            logFile = File(filePath)
            logFile?.parentFile?.mkdirs()
            initialized = true
            logInternal("TRACE", "OnMind-XDB Trace logging initialized: $filePath (level=$level)")
        } catch (e: Exception) {
            println("Failed to initialize trace logging: ${e.message}")
            initialized = false
        }
    }
    
    // Compatibilidad con versión anterior
    fun init(filePath: String) {
        init(filePath, 1)
    }
    
    // Método original para compatibilidad
    fun log(msg: String) {
        if (!initialized) {
            println(msg)
            return
        }
        
        synchronized(buffer) {
            val timestamp = LocalDateTime.now().format(formatter)
            buffer.add("$timestamp $msg")
            
            if (buffer.size >= flushThreshold || System.currentTimeMillis() - lastFlush > flushInterval) {
                flush()
            }
        }
    }
    
    // Nuevos métodos optimizados con inline
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logRequest(method: String, uri: String, status: Int) {
        if (!CoherenceConfig.shouldLogRequests()) return  // Early return, cero overhead
        
        val msg = "[$method] $uri -> $status"
        logInternal("REQUEST", msg)
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logDebug(msg: String) {
        if (!CoherenceConfig.shouldLogDebug()) return  // Early return, cero overhead
        
        logInternal("DEBUG", msg)
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logInfo(msg: String) {
        if (!CoherenceConfig.shouldLogDebug()) return  // Early return, cero overhead
        
        logInternal("INFO", msg)
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logWarn(msg: String) {
        if (!CoherenceConfig.shouldLogDebug()) return  // Early return, cero overhead
        
        logInternal("WARN", msg)
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logError(msg: String) {
        if (!CoherenceConfig.shouldLogDebug()) return  // Early return, cero overhead
        
        logInternal("ERROR", msg)
    }
    
    // ========== STRUCTURED LOGGING METHODS ==========
    
    /**
     * Logging estructurado con Map - Cero overhead cuando está desactivado
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun traceStructured(msg: String, fields: Map<String, Any>) {
        if (!CoherenceConfig.shouldLogDebug()) return
        logInternal("TRACE", formatStructured(msg, fields))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun debugStructured(msg: String, fields: Map<String, Any>) {
        if (!CoherenceConfig.shouldLogDebug()) return
        logInternal("DEBUG", formatStructured(msg, fields))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun infoStructured(msg: String, fields: Map<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        logInternal("INFO", formatStructured(msg, fields))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun warnStructured(msg: String, fields: Map<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        logInternal("WARN", formatStructured(msg, fields))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun errorStructured(msg: String, fields: Map<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        logInternal("ERROR", formatStructured(msg, fields))
    }
    
    /**
     * Logging estructurado con varargs - API simplificada
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun traceWith(msg: String, vararg pairs: Pair<String, Any>) {
        if (!CoherenceConfig.shouldLogDebug()) return
        traceStructured(msg, mapOf(*pairs))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun debugWith(msg: String, vararg pairs: Pair<String, Any>) {
        if (!CoherenceConfig.shouldLogDebug()) return
        debugStructured(msg, mapOf(*pairs))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun infoWith(msg: String, vararg pairs: Pair<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        infoStructured(msg, mapOf(*pairs))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun warnWith(msg: String, vararg pairs: Pair<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        warnStructured(msg, mapOf(*pairs))
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun errorWith(msg: String, vararg pairs: Pair<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        errorStructured(msg, mapOf(*pairs))
    }
    
    /**
     * Logging de errores con excepciones y contexto estructurado
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logError(msg: String, throwable: Throwable) {
        if (!CoherenceConfig.shouldLogRequests()) return
        
        logInternal("ERROR", "$msg: ${throwable.message}")
        if (CoherenceConfig.shouldLogDebug()) {
            logInternal("DEBUG", "Stack trace: ${throwable.stackTraceToString()}")
        }
    }
    
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logError(msg: String, throwable: Throwable, fields: Map<String, Any>) {
        if (!CoherenceConfig.shouldLogRequests()) return
        
        val errorFields = fields + mapOf(
            "exception" to throwable.javaClass.simpleName,
            "error_message" to (throwable.message ?: "No message")
        )
        errorStructured(msg, errorFields)
        if (CoherenceConfig.shouldLogDebug()) {
            logInternal("DEBUG", "Stack trace: ${throwable.stackTraceToString()}")
        }
    }
    
    /**
     * Métodos de conveniencia para patrones comunes de OnMind-XDB
     */
    
    // HTTP request logging optimizado para XDB
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logHttpRequest(method: String, path: String, status: Int, duration: Long? = null) {
        if (!CoherenceConfig.shouldLogRequests()) return
        
        val fields = mutableMapOf<String, Any>(
            "method" to method,
            "path" to path,
            "status" to status
        )
        duration?.let { fields["duration_ms"] = it }
        
        infoStructured("HTTP request", fields)
    }
    
    // Database operations logging
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logDatabaseOperation(operation: String, entity: String, success: Boolean, details: Map<String, Any> = emptyMap()) {
        if (!CoherenceConfig.shouldLogDebug()) return
        
        val fields = mutableMapOf<String, Any>(
            "operation" to operation,
            "entity" to entity,
            "success" to success
        ) + details
        
        if (success) {
            debugStructured("Database operation completed", fields)
        } else {
            warnStructured("Database operation failed", fields)
        }
    }
    
    // Coherence operations logging
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logCoherenceCheck(entity: String, memoryCount: Int, diskCount: Int, coherent: Boolean) {
        if (!CoherenceConfig.shouldLogDebug()) return
        
        val fields = mapOf(
            "entity" to entity,
            "memory_count" to memoryCount,
            "disk_count" to diskCount,
            "coherent" to coherent
        )
        
        if (coherent) {
            debugStructured("Coherence check OK", fields)
        } else {
            warnStructured("Coherence mismatch detected", fields)
        }
    }
    
    // Performance timing
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun logTiming(operation: String, duration: Long, details: Map<String, Any> = emptyMap()) {
        if (!CoherenceConfig.shouldLogDebug()) return
        
        val fields = mutableMapOf<String, Any>(
            "operation" to operation,
            "duration_ms" to duration
        ) + details
        
        debugStructured("Operation timing", fields)
    }
    
    /**
     * Formatear mensaje estructurado - Cero overhead cuando está desactivado
     */
    private fun formatStructured(msg: String, fields: Map<String, Any>): String {
        if (fields.isEmpty() || !structuredFormat) return msg
        
        val fieldsStr = fields.entries.joinToString(" ") { (key, value) ->
            when (value) {
                is String -> "$key=\"$value\""
                is Number -> "$key=$value"
                is Boolean -> "$key=$value"
                null -> "$key=null"
                else -> "$key=\"$value\""
            }
        }
        return "$msg | $fieldsStr"
    }
    
    internal fun logInternal(level: String, msg: String) {
        if (!initialized) return
        
        synchronized(buffer) {
            val timestamp = LocalDateTime.now().format(formatter)
            buffer.add("$timestamp [$level] $msg")
            
            if (buffer.size >= flushThreshold || System.currentTimeMillis() - lastFlush > flushInterval) {
                flush()
            }
        }
    }
    
    private fun flush() {
        if (buffer.isEmpty() || logFile == null) return
        
        try {
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                buffer.forEach { writer.write("$it\n") }
            }
            buffer.clear()
            lastFlush = System.currentTimeMillis()
        } catch (e: Exception) {
            println("Log flush failed: ${e.message}")
            buffer.forEach { println(it) }
            buffer.clear()
        }
    }
    
    fun shutdown() {
        synchronized(buffer) {
            if (initialized) {
                logInternal("TRACE", "OnMind-XDB Trace logging shutdown")
                flush()
            }
        }
    }
    
    fun getStats(): Map<String, Any> {
        return mapOf(
            "initialized" to initialized,
            "log_file" to (logFile?.absolutePath ?: "none"),
            "buffer_size" to buffer.size,
            "last_flush" to lastFlush,
            "flush_threshold" to flushThreshold,
            "flush_interval_ms" to flushInterval,
            "structured_format" to structuredFormat
        )
    }
}
