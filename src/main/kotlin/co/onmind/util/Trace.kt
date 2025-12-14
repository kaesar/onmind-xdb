package co.onmind.util

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Trace {
    private val buffer = mutableListOf<String>()
    private var logFile: File? = null
    private var lastFlush = System.currentTimeMillis()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val flushThreshold = 100
    private val flushInterval = 5000L // 5 seconds
    
    fun init(filePath: String) {
        logFile = File(filePath)
    }
    
    fun log(msg: String) {
        if (logFile == null) {
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
            flush()
        }
    }
}
