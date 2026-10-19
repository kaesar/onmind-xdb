package co.onmind.kv

import co.onmind.trait.KVStore
import co.onmind.kv.MVStorePlug
import co.onmind.util.Durability
import co.onmind.util.Rote
import java.util.*

object KVStoreFactory {

    fun createStore(config: Properties): KVStore {
        val store = createBackend(config)
        return applyDurability(store, config)
    }

    /**
     * Applies `kv.durability` (`sync` shape by default). `group`/`async` are only
     * honoured for backends that advertise [KVStore.supportsGroupCommit]; anything
     * else keeps the `sync` path, so the default behaviour is never altered.
     */
    private fun applyDurability(store: KVStore, config: Properties): KVStore {
        val mode = Rote.durabilityProp(config, "kv.durability")
        if (mode == Durability.SYNC) return store
        if (!store.supportsGroupCommit) {
            println("[WARN] kv.durability=$mode ignored: ${store.javaClass.simpleName} does not support group-commit (using sync)")
            return store
        }
        return GroupCommitStore(store, mode)
    }

    private fun createBackend(config: Properties): KVStore {
        val storeType = config.getProperty("kv.store", "mvstore").lowercase()

        return when (storeType) {
            "dynamodb" -> {
                try {
                    // init es vararg (compila a Object[]): llamada directa por interfaz.
                    // La reflexión con aridad fija no lo encuentra (NoSuchMethodException).
                    // Class.forName se conserva: en perfil lite la clase no existe.
                    val instance = Class.forName("co.onmind.kv.DynamoPlug")
                        .getDeclaredConstructor().newInstance() as KVStore
                    val tableName = config.getProperty("kv.dynamodb.table", "onmind-xdb")
                    val region = config.getProperty("kv.dynamodb.region", "us-east-1")
                    val autocreate = Rote.boolProp(config, "kv.dynamodb.autocreate", false)
                    val conditional = Rote.boolProp(config, "kv.dynamodb.conditional", false)
                    instance.init(tableName, region, autocreate, conditional)
                    instance
                } catch (e: ClassNotFoundException) {
                    throw IllegalArgumentException("DynamoDB support not available (lite profile)")
                }
            }
            "cosmosdb" -> {
                try {
                    // init es vararg (compila a Object[]): llamada directa por interfaz.
                    // La reflexión con aridad fija no lo encuentra (NoSuchMethodException).
                    // Class.forName se conserva: en perfil lite la clase no existe.
                    val instance = Class.forName("co.onmind.kv.CosmosPlug")
                        .getDeclaredConstructor().newInstance() as KVStore
                    val endpoint = config.getProperty("kv.cosmosdb.endpoint")
                        ?: throw IllegalArgumentException("kv.cosmosdb.endpoint is required")
                    val key = config.getProperty("kv.cosmosdb.key")
                        ?: throw IllegalArgumentException("kv.cosmosdb.key is required")
                    val database = config.getProperty("kv.cosmosdb.database", "onmindxdb")
                    val container = config.getProperty("kv.cosmosdb.container", "kvstore")
                    instance.init(endpoint, key, database, container)
                    instance
                } catch (e: ClassNotFoundException) {
                    throw IllegalArgumentException("CosmosDB support not available (lite profile)")
                }
            }
            "rocksdb" -> {
                try {
                    // init es vararg (compila a Object[]): llamada directa por interfaz.
                    // La reflexión con aridad fija no lo encuentra (NoSuchMethodException).
                    // Class.forName se conserva: en perfil lite la clase no existe.
                    val instance = Class.forName("co.onmind.kv.RocksDBPlug")
                        .getDeclaredConstructor().newInstance() as KVStore
                    val path = config.getProperty("kv.rocksdb.path", "/tmp/rocksdb")
                    instance.init(path)
                    instance
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

