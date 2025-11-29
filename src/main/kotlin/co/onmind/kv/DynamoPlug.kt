package co.onmind.kv

import co.onmind.trait.KVStore
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.regions.Region

class DynamoPlug: KVStore {

    private var tableName: String? = null
    private var dynamoDbClient: DynamoDbClient? = null

    override fun init(vararg params: Any?) {
        tableName = params[1] as String?
        dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_EAST_1)  // Cambia la región según sea necesario
            .build()
    }

    override fun put(key: String, value: String) {
        val item = mapOf(
            "Key" to AttributeValue.builder().s(key).build(),
            "Value" to AttributeValue.builder().s(value).build()
        )

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build()

        dynamoDbClient?.putItem(request)
    }

    override fun get(key: String): String? {
        val keyToGet = mapOf(
            "Key" to AttributeValue.builder().s(key).build()
        )

        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(keyToGet)
            .build()

        val returnedItem = dynamoDbClient?.getItem(request)?.item()

        return returnedItem?.get("Value")?.s()
    }

    override fun delete(key: String) {
        val keyToDelete = mapOf(
            "Key" to AttributeValue.builder().s(key).build()
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
            val key = item["Key"]?.s()
            val value = item["Value"]?.s()
            if (key != null && value != null) {
                action(key, value)
            }
        }
    }
}