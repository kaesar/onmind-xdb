package co.onmind.kv

import co.onmind.trait.KVStore
import co.onmind.kv.MVStorePlug
import co.onmind.kv.DynamoPlug
import co.onmind.kv.CosmosPlug
//import co.onmind.kv.RocksDBPlug
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
            "cosmosdb" -> {
                CosmosPlug().apply {
                    val endpoint   = config.getProperty("kv.cosmosdb.endpoint")
                        ?: throw IllegalArgumentException("kv.cosmosdb.endpoint is required")
                    val key        = config.getProperty("kv.cosmosdb.key")
                        ?: throw IllegalArgumentException("kv.cosmosdb.key is required")
                    val database   = config.getProperty("kv.cosmosdb.database", "onmindxdb")
                    val container  = config.getProperty("kv.cosmosdb.container", "kvstore")
                    init(endpoint, key, database, container)
                }
            }
            //"rocksdb" -> {
            //    RocksDBPlug().apply {
            //        val path          = config.getProperty("kv.rocksdb.path", "/tmp/rocksdb")
            //        val compression   = config.getProperty("kv.rocksdb.compression")
            //        init(path, if (compression != null) compression else null)   // optional compression
            //    }
            //}
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
