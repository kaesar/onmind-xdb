package co.onmind.kv

import co.onmind.trait.KVStore
import co.onmind.kv.MVStorePlug
import java.util.*

object KVStoreFactory {

    fun createStore(config: Properties): KVStore {
        val storeType = config.getProperty("kv.store", "mvstore").lowercase()

        return when (storeType) {
            "dynamodb" -> {
                try {
                    val clazz = Class.forName("co.onmind.kv.DynamoPlug")
                    val instance = clazz.getDeclaredConstructor().newInstance()
                    val initMethod = clazz.getMethod("init", Any::class.java, Any::class.java)
                    val tableName = config.getProperty("kv.dynamodb.table", "onmind-xdb")
                    val region = config.getProperty("kv.dynamodb.region", "us-east-1")
                    initMethod.invoke(instance, tableName, region)
                    instance as KVStore
                } catch (e: ClassNotFoundException) {
                    throw IllegalArgumentException("DynamoDB support not available (lite profile)")
                }
            }
            "cosmosdb" -> {
                try {
                    val clazz = Class.forName("co.onmind.kv.CosmosPlug")
                    val instance = clazz.getDeclaredConstructor().newInstance()
                    val initMethod = clazz.getMethod("init", Any::class.java, Any::class.java, Any::class.java, Any::class.java)
                    val endpoint = config.getProperty("kv.cosmosdb.endpoint")
                        ?: throw IllegalArgumentException("kv.cosmosdb.endpoint is required")
                    val key = config.getProperty("kv.cosmosdb.key")
                        ?: throw IllegalArgumentException("kv.cosmosdb.key is required")
                    val database = config.getProperty("kv.cosmosdb.database", "onmindxdb")
                    val container = config.getProperty("kv.cosmosdb.container", "kvstore")
                    initMethod.invoke(instance, endpoint, key, database, container)
                    instance as KVStore
                } catch (e: ClassNotFoundException) {
                    throw IllegalArgumentException("CosmosDB support not available (lite profile)")
                }
            }
            "rocksdb" -> {
                try {
                    val clazz = Class.forName("co.onmind.kv.RocksDBPlug")
                    val instance = clazz.getDeclaredConstructor().newInstance()
                    val initMethod = clazz.getMethod("init", Any::class.java)
                    val path = config.getProperty("kv.rocksdb.path", "/tmp/rocksdb")
                    initMethod.invoke(instance, path)
                    instance as KVStore
                } catch (e: ClassNotFoundException) {
                    throw IllegalArgumentException("RocksDB support not available (lite profile)")
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
