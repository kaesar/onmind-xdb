package co.onmind.util

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Sistema de Trace optimizado para OnMind-XDB
 * Garantiza cero overhead cuando está desactivado
 */
object Trace {
    private val buffer = mutableListOf<String>()
    private var logFile: File? = null
    private var lastFlush = System.currentTimeMillis()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val flushThreshold = 100
    private val flushInterval = 5000L // 5 seconds
    private var initialized = false
    
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
            "flush_interval_ms" to flushInterval
        )
    }
}
