package co.onmind.kv

import co.onmind.trait.KVStore
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.regions.Region

class DynamoPlug: KVStore {

    private var tableName: String? = null
    private var dynamoDbClient: DynamoDbClient? = null

    /**
     * Convierte clave interna (~) a formato DynamoDB (#)
     */
    private fun toExternalKey(internalKey: String): String {
        return internalKey.replace("~", "#")
    }
    
    /**
     * Convierte clave DynamoDB (#) a formato interno (~)
     */
    private fun toInternalKey(externalKey: String): String {
        return externalKey.replace("#", "~")
    }

    override fun init(vararg params: Any?) {
        tableName = params[1] as String?
        dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_EAST_1)  // Cambia la región según sea necesario
            .build()
    }

    override fun put(key: String, value: String) {
        val dynamoKey = toExternalKey(key)  // "abc123~kit~box" -> "abc123#kit#box"
        val item = mapOf(
            "Key" to AttributeValue.builder().s(dynamoKey).build(),
            "Value" to AttributeValue.builder().s(value).build()
        )

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build()

        dynamoDbClient?.putItem(request)
    }

    override fun get(key: String): String? {
        val dynamoKey = toExternalKey(key)  // "abc123~kit~box" -> "abc123#kit#box"
        val keyToGet = mapOf(
            "Key" to AttributeValue.builder().s(dynamoKey).build()
        )

        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(keyToGet)
            .build()

        val returnedItem = dynamoDbClient?.getItem(request)?.item()

        return returnedItem?.get("Value")?.s()
    }

    override fun delete(key: String) {
        val dynamoKey = toExternalKey(key)  // "abc123~kit~box" -> "abc123#kit#box"
        val keyToDelete = mapOf(
            "Key" to AttributeValue.builder().s(dynamoKey).build()
        )

        val request = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(keyToDelete)
            .build()

        dynamoDbClient?.deleteItem(request)
    }

    override fun commit() {
        // DynamoDB no requiere commit explícito
    }

    override fun close() {
        dynamoDbClient?.close()
    }
    
    override fun forEach(action: (String, String) -> Unit) {
        val request = ScanRequest.builder()
            .tableName(tableName)
            .build()
        
        val response = dynamoDbClient?.scan(request)
        response?.items()?.forEach { item ->
            val dynamoKey = item["Key"]?.s()  // "abc123#kit#box"
            val value = item["Value"]?.s()
            if (dynamoKey != null && value != null) {
                val internalKey = toInternalKey(dynamoKey)  // "abc123#kit#box" -> "abc123~kit~box"
                action(internalKey, value)
            }
        }
    }
}