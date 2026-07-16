package co.onmind.util

import co.onmind.kv.KVStoreFactory
import co.onmind.trait.KVStore
import co.onmind.db.RDB
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Coherence system for OnMind-XDB
 * Verifies coherence between H2 (memory) and KVStore (disk)
 * Includes incremental O(1) counters to avoid full-scan on disk
 */
object CoherenceStore {
    private val lock = ReentrantReadWriteLock()
    private var rdb: RDB? = null
    
    // Incremental counters in memory (O(1) for getDiskCount)
    // Key: entityType (any, key, set, kit, doc)
    private val memoryCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val diskCounts = ConcurrentHashMap<String, AtomicInteger>()
    
    fun init(rdbInstance: RDB) {
        rdb = rdbInstance
        // Initialize counters to zero for each entity
        listOf("any", "key", "set", "kit", "doc").forEach { entity ->
            memoryCounts.putIfAbsent(entity, AtomicInteger(0))
            diskCounts.putIfAbsent(entity, AtomicInteger(0))
        }
        // Do not create a new KVStore instance, use the one from RDB
        Trace.debugWith("CoherenceStore initialized", "rdb_instance" to "available", "counters_initialized" to true)
    }
    
    /**
     * Increment memory counter (called after successful INSERT/UPDATE in H2)
     */
    fun incrementMemoryCount(entityType: String) {
        memoryCounts[entityType]?.incrementAndGet()
    }
    
    /**
     * Decrement memory counter (called after successful DELETE in H2)
     */
    fun decrementMemoryCount(entityType: String) {
        memoryCounts[entityType]?.decrementAndGet()
    }
    
    /**
     * Increment disk counter (called after successful PUT in KVStore)
     */
    fun incrementDiskCount(entityType: String) {
        diskCounts[entityType]?.incrementAndGet()
    }
    
    /**
     * Decrement disk counter (called after successful DELETE in KVStore)
     */
    fun decrementDiskCount(entityType: String) {
        diskCounts[entityType]?.decrementAndGet()
    }
    
    /**
     * Synchronize counters with actual state (called in forceSyncFromDisk and at startup)
     */
    fun resyncCounters() {
        lock.write {
            listOf("any", "key", "set", "kit", "doc").forEach { entity ->
                val memCount = getMemoryCountSlow(entity) // Full scan SQL for memory
                val diskCount = getDiskCountSlow(entity) // Full scan only here for disk
                memoryCounts[entity]?.set(memCount)
                diskCounts[entity]?.set(diskCount)
            }
            Trace.debugWith("Coherence counters resynced", "memory" to memoryCounts.mapValues { it.value.get() }, "disk" to diskCounts.mapValues { it.value.get() })
        }
    }
    
    /**
     * Quick coherence check (counters only, O(1))
     * Zero overhead if db.check is disabled
     * Uses lock.read for atomicity between memoryCount and diskCount (shared lock, doesn't block other readers)
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun verifyCoherenceQuick(entityType: String) {
        if (!CoherenceConfig.shouldCheckCoherence()) return  // Early return, zero overhead
        
        val startTime = System.currentTimeMillis()
        try {
            // Use lock.read for atomicity between memoryCount and diskCount (shared lock, doesn't block other readers)
            lock.read {
                val memoryCount = getMemoryCount(entityType)
                val diskCount = getDiskCount(entityType)  // O(1) with incremental counter
                
                if (memoryCount != diskCount) {
                    Trace.logCoherenceCheck(entityType, memoryCount, diskCount, false)
                } else {
                    Trace.logCoherenceCheck(entityType, memoryCount, diskCount, true)
                }
            }
        } catch (e: Exception) {
            Trace.errorWith("Coherence check failed", 
                "entity" to entityType, 
                "error" to (e.message ?: "Unknown error")
            )
        } finally {
            val duration = System.currentTimeMillis() - startTime
            if (duration > 10) {  // Log only if takes >10ms
                Trace.logTiming("verifyCoherenceQuick", duration, mapOf("entity" to entityType))
            }
        }
    }
    
    /**
     * Get complete coherence statistics (on demand)
     */
    fun getCoherenceStats(): Map<String, Any> {
        return lock.read {
            try {
                val entities = listOf("any", "key", "set", "kit", "doc")
                val stats = mutableMapOf<String, Any>()
                
                entities.forEach { entity ->
                    val memoryCount = getMemoryCount(entity)
                    val diskCount = getDiskCount(entity)
                    val coherent = memoryCount == diskCount
                    
                    stats[entity] = mapOf(
                        "memory_count" to memoryCount,
                        "disk_count" to diskCount,
                        "coherent" to coherent
                    )
                }
                
                val overallCoherent = stats.values.all { 
                    (it as Map<*, *>)["coherent"] as Boolean 
                }
                
                mapOf(
                    "overall_coherent" to overallCoherent,
                    "entities" to stats,
                    "last_check" to System.currentTimeMillis(),
                    "config" to CoherenceConfig.getStats()
                )
            } catch (e: Exception) {
                mapOf(
                    "error" to (e.message ?: "Unknown error"),
                    "overall_coherent" to false,
                    "last_check" to System.currentTimeMillis()
                )
            }
        }
    }
    
    /**
     * Verificación completa de coherencia (bajo demanda)
     * Usa lock.read para consistencia, métrica de latencia incluida
     */
    fun verifyCoherence(): Boolean {
        val startTime = System.currentTimeMillis()
        return lock.read {
            try {
                val entities = listOf("any", "key", "set", "kit", "doc")
                var allCoherent = true
                
                entities.forEach { entity ->
                    val memoryCount = getMemoryCount(entity)
                    val diskCount = getDiskCount(entity)
                    val coherent = memoryCount == diskCount
                    
                    if (!coherent) {
                        allCoherent = false
                        Trace.logCoherenceCheck(entity, memoryCount, diskCount, false)
                    } else {
                        Trace.logCoherenceCheck(entity, memoryCount, diskCount, true)
                    }
                }
                
                if (allCoherent) {
                    Trace.infoWith("Data coherence verification completed", 
                        "result" to "success", 
                        "entities_checked" to entities.size
                    )
                } else {
                    Trace.warnWith("Data coherence verification completed", 
                        "result" to "failed", 
                        "entities_checked" to entities.size,
                        "issue" to "memory_disk_mismatch"
                    )
                }
                
                val duration = System.currentTimeMillis() - startTime
                Trace.logTiming("verifyCoherence", duration, mapOf("result" to allCoherent, "entities" to entities.size))
                
                allCoherent
            } catch (e: Exception) {
                Trace.errorWith("Coherence verification failed", 
                    "error" to (e.message ?: "Unknown error"),
                    "operation" to "verify_coherence"
                )
                false
            }
        }
    }
    
    /**
     * Forzar sincronización desde disco (recuperación)
     * Resincroniza contadores incrementales tras reload completo
     */
    fun forceSyncFromDisk(): Boolean {
        val startTime = System.currentTimeMillis()
        return lock.write {
            try {
                Trace.warnWith("Force synchronization initiated", 
                    "operation" to "sync_from_disk",
                    "reason" to "manual_request"
                )
                
                // Reinicializar RDB desde KVStore
                rdb?.readPoint()
                
                // Resincronizar contadores incrementales (full scan solo aquí)
                resyncCounters()
                
                val coherent = verifyCoherence()
                if (coherent) {
                    Trace.infoWith("Force synchronization completed", 
                        "result" to "success",
                        "coherence_status" to "synchronized"
                    )
                } else {
                    Trace.errorWith("Force synchronization completed", 
                        "result" to "partial_success",
                        "coherence_status" to "issues_remain"
                    )
                }
                
                val duration = System.currentTimeMillis() - startTime
                Trace.logTiming("forceSyncFromDisk", duration, mapOf("result" to coherent))
                
                coherent
            } catch (e: Exception) {
                Trace.errorWith("Force synchronization failed", 
                    "error" to (e.message ?: "Unknown error"),
                    "operation" to "sync_from_disk"
                )
                false
            }
        }
    }
    
    /**
     * Obtener conteo de elementos en memoria (H2) - O(1) desde contador incremental
     */
    internal fun getMemoryCount(entityType: String): Int {
        return memoryCounts[entityType]?.get() ?: 0
    }
    
    /**
     * Obtener conteo real en memoria via SQL (para resyncCounters)
     */
    private fun getMemoryCountSlow(entityType: String): Int {
        return try {
            val sql = when (entityType) {
                "any" -> "SELECT COUNT(*) as count FROM xyany"
                "key" -> "SELECT COUNT(*) as count FROM xykey"
                "set" -> "SELECT COUNT(*) as count FROM xyset"
                "kit" -> "SELECT COUNT(*) as count FROM xykit"
                "doc" -> "SELECT COUNT(*) as count FROM xydoc"
                else -> return 0
            }
            
            val result = rdb?.forQuery(sql)
            (result?.firstOrNull()?.get("count") as? Number)?.toInt() ?: 0
        } catch (e: Exception) {
            Trace.errorWith("Failed to get memory count", 
                "entity" to entityType, 
                "error" to (e.message ?: "Unknown error"),
                "operation" to "memory_count_query"
            )
            0
        }
    }
    
    /**
     * Obtener conteo de elementos en disco (KVStore) - O(1) desde contador incremental
     * Evita full-scan del KVStore en cada verificación
     */
    internal fun getDiskCount(entityType: String): Int {
        return diskCounts[entityType]?.get() ?: 0
    }
    
    /**
     * Obtener conteo real en disco via full-scan (para resyncCounters inicial)
     */
    private fun getDiskCountSlow(entityType: String): Int {
        return try {
            var count = 0
            val store = RDB.getStoreInstance()
            store?.forEach { key, _ ->
                if (key.contains("~${entityType}~")) {
                    count++
                }
            }
            count
        } catch (e: Exception) {
            Trace.errorWith("Failed to get disk count", 
                "entity" to entityType, 
                "error" to (e.message ?: "Unknown error"),
                "operation" to "disk_count_query"
            )
            0
        }
    }
    
    /**
     * Verificación de salud del sistema
     */
    fun healthCheck(): Map<String, Any> {
        return try {
            val kvStoreHealthy = RDB.getStoreInstance() != null
            val rdbHealthy = rdb != null
            val configHealthy = onmindxdb.config != null
            
            mapOf(
                "healthy" to (kvStoreHealthy && rdbHealthy && configHealthy),
                "kvstore_accessible" to kvStoreHealthy,
                "rdb_accessible" to rdbHealthy,
                "config_loaded" to configHealthy,
                "coherence_config" to CoherenceConfig.getStats(),
                "trace_stats" to Trace.getStats()
            )
        } catch (e: Exception) {
            mapOf(
                "healthy" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }
}