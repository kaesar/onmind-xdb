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
        Trace.logDebug("CoherenceStore initialized")
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
                Trace.logWarn("Coherence mismatch for $entityType: memory=$memoryCount, disk=$diskCount")
            } else {
                Trace.logDebug("Coherence OK for $entityType: $memoryCount items")
            }
        } catch (e: Exception) {
            Trace.logError("Coherence check failed for $entityType: ${e.message}")
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
                        Trace.logWarn("Coherence check failed for $entity: memory=$memoryCount, disk=$diskCount")
                    } else {
                        Trace.logDebug("Coherence check passed for $entity: $memoryCount items")
                    }
                }
                
                if (allCoherent) {
                    Trace.logInfo("Data coherence check passed - Memory and disk are synchronized")
                } else {
                    Trace.logWarn("Data coherence check failed - Memory/Disk mismatch detected")
                }
                
                allCoherent
            } catch (e: Exception) {
                Trace.logError("Coherence verification failed: ${e.message}")
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
                Trace.logWarn("Forcing synchronization from disk to memory")
                
                // Reinicializar RDB desde KVStore
                rdb?.readPoint()
                
                val coherent = verifyCoherence()
                if (coherent) {
                    Trace.logInfo("Force sync completed successfully")
                } else {
                    Trace.logError("Force sync completed but coherence issues remain")
                }
                coherent
            } catch (e: Exception) {
                Trace.logError("Force sync failed: ${e.message}")
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
            Trace.logError("Failed to get memory count for $entityType: ${e.message}")
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
            Trace.logError("Failed to get disk count for $entityType: ${e.message}")
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