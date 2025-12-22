package co.onmind.util

import co.onmind.kv.KVStoreFactory
import co.onmind.trait.KVStore
import co.onmind.db.RDB
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Sistema de coherencia para OnMind-XDB
 * Verifica la coherencia entre H2 (memoria) y KVStore (disco)
 */
object CoherenceStore {
    private val lock = ReentrantReadWriteLock()
    private var rdb: RDB? = null
    
    fun init(rdbInstance: RDB) {
        rdb = rdbInstance
        // No crear una nueva instancia de KVStore, usar la del RDB
        Trace.debugWith("CoherenceStore initialized", "rdb_instance" to "available")
    }
    
    /**
     * Verificación rápida de coherencia (solo contadores)
     * Cero overhead si db.check está desactivado
     */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun verifyCoherenceQuick(entityType: String) {
        if (!CoherenceConfig.shouldCheckCoherence()) return  // Early return, cero overhead
        
        try {
            val memoryCount = getMemoryCount(entityType)
            val diskCount = getDiskCount(entityType)
            
            if (memoryCount != diskCount) {
                Trace.logCoherenceCheck(entityType, memoryCount, diskCount, false)
            } else {
                Trace.logCoherenceCheck(entityType, memoryCount, diskCount, true)
            }
        } catch (e: Exception) {
            Trace.errorWith("Coherence check failed", 
                "entity" to entityType, 
                "error" to (e.message ?: "Unknown error")
            )
        }
    }
    
    /**
     * Obtener estadísticas completas de coherencia (bajo demanda)
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
     * Verificación completa de coherencia
     */
    fun verifyCoherence(): Boolean {
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
     */
    fun forceSyncFromDisk(): Boolean {
        return lock.write {
            try {
                Trace.warnWith("Force synchronization initiated", 
                    "operation" to "sync_from_disk",
                    "reason" to "manual_request"
                )
                
                // Reinicializar RDB desde KVStore
                rdb?.readPoint()
                
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
     * Obtener conteo de elementos en memoria (H2)
     */
    internal fun getMemoryCount(entityType: String): Int {
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
     * Obtener conteo de elementos en disco (KVStore)
     */
    internal fun getDiskCount(entityType: String): Int {
        return try {
            var count = 0
            // Usar el store del RDB en lugar de crear uno nuevo
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