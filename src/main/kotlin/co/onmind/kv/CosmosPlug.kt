package co.onmind.kv

import co.onmind.trait.KVStore
import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.CosmosContainer
import com.azure.cosmos.CosmosDatabase
import com.azure.cosmos.models.CosmosItemRequestOptions
import com.azure.cosmos.models.CosmosQueryRequestOptions
import com.azure.cosmos.models.PartitionKey

class CosmosPlug : KVStore {

    private var client: CosmosClient? = null
    private var database: CosmosDatabase? = null
    private var container: CosmosContainer? = null

    /**
     * Maps internal key format (~) to Cosmos DB document id format (#)
     */
    private fun toDocId(internalKey: String): String {
        return internalKey.replace("~", "#")
    }

    /**
     * Maps Cosmos DB document id format (#) back to internal key format (~)
     */
    private fun toInternalKey(docId: String): String {
        return docId.replace("#", "~")
    }

    override fun init(vararg params: Any?) {
        val endpoint = params[0] as? String
            ?: throw IllegalArgumentException("Cosmos DB endpoint is required")
        val key = params[1] as? String
            ?: throw IllegalArgumentException("Cosmos DB key is required")
        val databaseId = params[2] as? String ?: "onmindxdb"
        val containerId = params[3] as? String ?: "kvstore"

        client = CosmosClientBuilder()
            .endpoint(endpoint)
            .key(key)
            .buildClient()

        database = client?.getDatabase(databaseId)
        container = database?.getContainer(containerId)
    }

    override fun put(key: String, value: String) {
        val docId = toDocId(key)
        val document = mapOf(
            "id" to docId,
            "key" to docId,
            "value" to value
        )

        container?.upsertItem(
            document,
            PartitionKey(docId),
            CosmosItemRequestOptions()
        )
    }

    override fun get(key: String): String? {
        val docId = toDocId(key)
        return try {
            container?.readItem(docId, PartitionKey(docId), Map::class.java)
                ?.item?.get("value")?.toString()
        } catch (e: Exception) {
            null
        }
    }

    override fun delete(key: String) {
        val docId = toDocId(key)
        try {
            container?.deleteItem(docId, PartitionKey(docId), CosmosItemRequestOptions())
        } catch (e: Exception) {
            // Ignore if item doesn't exist
        }
    }

    override fun commit() {
        // Cosmos DB handles consistency automatically - no-op
    }

    override fun close() {
        client?.close()
    }

    override fun forEach(action: (String, String) -> Unit) {
        val query = "SELECT c.key, c.value FROM c"
        val queryOptions = CosmosQueryRequestOptions()

        val results = container?.queryItems(query, queryOptions, Map::class.java)
        results?.forEach { item ->
            val docKey = item["key"]?.toString()
            val value = item["value"]?.toString()
            if (docKey != null && value != null) {
                val internalKey = toInternalKey(docKey)
                action(internalKey, value)
            }
        }
    }
}
