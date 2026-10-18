package co.onmind.kv

import co.onmind.trait.KVStore
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.WriteRequest

/**
 * AWS DynamoDB backend for the pluggable KVStore (full profile only).
 *
 * Layout: a single table (default `onmind-xdb`) with a string partition key
 * `Key` and a string `Value` holding the JSON payload. Keys are stored with
 * `~` replaced by `#` (cosmetic; both are legal, but `#` avoids any `${...}`
 * surprises in tooling).
 *
 * Writes are buffered and flushed on [commit] / [close] / [forEach] (XDB always
 * calls `put`/`delete` immediately followed by `commit`). The default flush uses
 * `BatchWriteItem` (25 items per request) and retries `UnprocessedItems`, so a
 * bulk of writes never loses data silently. When opt-in optimistic concurrency
 * is enabled a `Version` attribute plus a conditional `PutItem` makes concurrent
 * writers "newest wins" instead of "last arrival wins".
 */
class DynamoPlug : KVStore {

    private companion object {
        const val ITEM_LIMIT_BYTES = 400_000          // DynamoDB hard limit is 400 KB per item
        const val BATCH_MAX = 25                      // BatchWriteItem: at most 25 requests
        const val UNPROCESSED_MAX_ATTEMPTS = 5        // retries for UnprocessedItems
        const val PROJECTION = "#k, #v"
    }

    private var tableName: String? = null
    private var autocreate: Boolean = false
    private var conditional: Boolean = false
    private var tableReady: Boolean = false
    private var dynamoDbClient: DynamoDbClient? = null

    // ---- write buffer (XDB: put/delete followed by commit) ----

    private sealed interface Op
    private class PutOp(val value: String, val version: Long) : Op
    private class DeleteOp(val version: Long) : Op

    private val lock = Any()
    private val pending = LinkedHashMap<String, Op>()

    override fun init(vararg params: Any?) {
        val table = params.getOrNull(0) as? String
            ?: throw IllegalArgumentException("DynamoDB table name is required (kv.dynamodb.table)")
        val region = (params.getOrNull(1) as? String)?.takeIf { it.isNotBlank() } ?: "us-east-1"
        autocreate = params.getOrNull(2) as? Boolean ?: false
        conditional = params.getOrNull(3) as? Boolean ?: false

        tableName = table
        dynamoDbClient = DynamoDbClient.builder().region(Region.of(region)).build()

        // Fail fast at startup only when we are allowed to create the table.
        if (autocreate) ensureTable()
    }

    // ---- key mapping ----

    internal fun toExternalKey(internalKey: String): String = internalKey.replace("~", "#")

    internal fun toInternalKey(externalKey: String): String = externalKey.replace("#", "~")

    // ---- KVStore ----

    override fun put(key: String, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8).size
        if (bytes > ITEM_LIMIT_BYTES) {
            throw IllegalArgumentException(
                "Value for key '$key' is $bytes bytes and exceeds the DynamoDB 400 KB item limit"
            )
        }
        synchronized(lock) { pending[key] = PutOp(value, System.currentTimeMillis()) }
    }

    override fun get(key: String): String? {
        synchronized(lock) {
            when (val op = pending[key]) {
                is PutOp -> return op.value
                is DeleteOp -> return null
                null -> {}
            }
        }
        ensureTable()
        val request = GetItemRequest.builder()
            .tableName(requireTable())
            .key(mapOf("Key" to AttributeValue.builder().s(toExternalKey(key)).build()))
            .consistentRead(true)
            .build()
        return requireClient().getItem(request).item()?.get("Value")?.s()
    }

    override fun delete(key: String) {
        synchronized(lock) { pending[key] = DeleteOp(System.currentTimeMillis()) }
    }

    override fun commit() = flush()

    override fun close() {
        try {
            flush()
        } finally {
            dynamoDbClient?.close()
            dynamoDbClient = null
        }
    }

    override fun forEach(action: (String, String) -> Unit) {
        flush()
        ensureTable()
        val client = requireClient()
        val table = requireTable()
        var startKey: Map<String, AttributeValue>? = null
        do {
            val builder = ScanRequest.builder()
                .tableName(table)
                .projectionExpression(PROJECTION)
                .expressionAttributeNames(mapOf("#k" to "Key", "#v" to "Value"))
            if (startKey != null) builder.exclusiveStartKey(startKey)
            val response = client.scan(builder.build())
            response.items().forEach { item ->
                val dynamoKey = item["Key"]?.s()
                val value = item["Value"]?.s()
                if (dynamoKey != null && value != null) action(toInternalKey(dynamoKey), value)
            }
            startKey = response.lastEvaluatedKey()?.takeIf { it.isNotEmpty() }
        } while (startKey != null)
    }

    // ---- flush ----

    private fun flush() {
        val snapshot: Map<String, Op>
        synchronized(lock) {
            if (pending.isEmpty()) return
            snapshot = LinkedHashMap(pending)
            pending.clear()
        }
        try {
            ensureTable()
            if (conditional) flushConditional(snapshot) else flushBatch(snapshot)
        } catch (e: Exception) {
            // Preserve un-flushed items so a later commit() can retry them.
            synchronized(lock) { for ((key, op) in snapshot) pending.putIfAbsent(key, op) }
            throw e
        }
    }

    private fun flushBatch(ops: Map<String, Op>) {
        val table = requireTable()
        val writes = ops.entries.map { (key, op) ->
            val dynamoKey = toExternalKey(key)
            when (op) {
                is PutOp -> WriteRequest.builder().putRequest(
                    PutRequest.builder().item(
                        mapOf(
                            "Key" to AttributeValue.builder().s(dynamoKey).build(),
                            "Value" to AttributeValue.builder().s(op.value).build()
                        )
                    ).build()
                ).build()
                is DeleteOp -> WriteRequest.builder().deleteRequest(
                    DeleteRequest.builder()
                        .key(mapOf("Key" to AttributeValue.builder().s(dynamoKey).build()))
                        .build()
                ).build()
            }
        }
        for (chunk in writes.chunked(BATCH_MAX)) writeChunkWithRetry(table, chunk)
    }

    private fun writeChunkWithRetry(table: String, initial: List<WriteRequest>) {
        val client = requireClient()
        var request = initial
        var attempt = 0
        while (request.isNotEmpty() && attempt < UNPROCESSED_MAX_ATTEMPTS) {
            val response = client.batchWriteItem(
                BatchWriteItemRequest.builder().requestItems(mapOf(table to request)).build()
            )
            request = response.unprocessedItems()[table].orEmpty()
            if (request.isEmpty()) return
            attempt++
            Thread.sleep((50L shl attempt).coerceAtMost(2_000L))
        }
        if (request.isNotEmpty()) {
            throw IllegalStateException(
                "DynamoDB BatchWriteItem left ${request.size} unprocessed item(s) after $UNPROCESSED_MAX_ATTEMPTS attempts"
            )
        }
    }

    private fun flushConditional(ops: Map<String, Op>) {
        val client = requireClient()
        val table = requireTable()
        for ((key, op) in ops) {
            val dynamoKey = toExternalKey(key)
            when (op) {
                is PutOp -> {
                    val version = op.version.toString()
                    try {
                        client.putItem(
                            PutItemRequest.builder()
                                .tableName(table)
                                .item(
                                    mapOf(
                                        "Key" to AttributeValue.builder().s(dynamoKey).build(),
                                        "Value" to AttributeValue.builder().s(op.value).build(),
                                        "Version" to AttributeValue.builder().n(version).build()
                                    )
                                )
                                .conditionExpression("attribute_not_exists(#v) OR #v <= :new")
                                .expressionAttributeNames(mapOf("#v" to "Version"))
                                .expressionAttributeValues(
                                    mapOf(":new" to AttributeValue.builder().n(version).build())
                                )
                                .build()
                        )
                    } catch (e: ConditionalCheckFailedException) {
                        // A newer version already exists: keep the newer data instead of clobbering it.
                        System.err.println(
                            "[DynamoPlug] stale write ignored for key '$key' (version $version)"
                        )
                    }
                }
                is DeleteOp -> client.deleteItem(
                    DeleteItemRequest.builder()
                        .tableName(table)
                        .key(mapOf("Key" to AttributeValue.builder().s(dynamoKey).build()))
                        .build()
                )
            }
        }
    }

    // ---- helpers ----

    private fun requireClient(): DynamoDbClient =
        dynamoDbClient ?: throw IllegalStateException("DynamoPlug not initialised")

    private fun requireTable(): String =
        tableName ?: throw IllegalStateException("DynamoPlug not initialised")

    private fun ensureTable() {
        if (tableReady) return
        val client = requireClient()
        val table = requireTable()
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(table).build())
            tableReady = true
        } catch (e: ResourceNotFoundException) {
            if (!autocreate) {
                throw IllegalStateException(
                    "DynamoDB table '$table' does not exist " +
                        "(create it, or set kv.dynamodb.autocreate = true)"
                )
            }
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(table)
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            .attributeName("Key").attributeType(ScalarAttributeType.S).build()
                    )
                    .keySchema(
                        KeySchemaElement.builder().attributeName("Key").keyType(KeyType.HASH).build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build()
            )
            client.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(table).build())
            tableReady = true
        }
    }
}
