package co.onmind.kv

import co.onmind.trait.KVStore
import java.util.*

object KVStoreFactory {
    
    fun createStore(config: Properties): KVStore {
        val storeType = config.getProperty("kv.store", "mvstore").lowercase()
        
        return when (storeType) {
            "dynamodb" -> {
                DynamoPlug().apply {
                    val tableName = config.getProperty("kv.dynamodb.table", "onmind-xdb")
                    val region = config.getProperty("kv.dynamodb.region", "us-east-1")
                    init(tableName, region)
                }
            }
            "ehcache" -> {
                EhCachePlug().apply {
                    val cacheName = config.getProperty("kv.ehcache.name", "xybox")
                    val maxEntries = config.getProperty("kv.ehcache.max_entries", "10000")
                    init(cacheName, maxEntries)
                }
            }
            else -> {
                MVStorePlug().apply {
                    val fileName = onmindxdb.dbfile
                    val storeName = config.getProperty("kv.mvstore.name", "xybox")
                    init(fileName, storeName)
                }
            }
        }
    }
}