package co.onmind.mcp

import co.onmind.util.JsonMapper
import co.onmind.util.Trace
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/**
 * Minimal MCP (JSON-RPC 2.0) server for OnMind-XDB.
 *
 * Zero third-party MCP SDK: reuses Jackson already on the classpath.
 * Supports:
 *  - HTTP POST /mcp  (single JSON-RPC request/response)
 *  - stdio newline-delimited JSON-RPC (flag --mcp or mcp.stdio=+)
 *
 * Protocol surface: initialize, notifications/initialized, tools/list, tools/call, ping.
 */
class AbcMcpServer(
    private val tools: AbcMcpTools = AbcMcpTools(),
    private val protocolVersion: String = "2024-11-05"
) {
    private val mapper = JsonMapper.instance

    fun httpHandler(): HttpHandler = { request: Request ->
        when (request.method) {
            Method.GET -> {
                // Lightweight discovery for humans/UIs (not full MCP streamable HTTP).
                val catalog = tools.catalog()
                val info = mapOf(
                    "ok" to true,
                    "service" to "OnMind-XDB MCP",
                    "transport" to "json-rpc",
                    "endpoint" to "/mcp",
                    "protocolVersion" to protocolVersion,
                    "mode" to tools.modeLabel(),
                    "write" to tools.writeEnabled,
                    "tools" to catalog.map { mapOf("name" to it.name, "write" to it.write) },
                    "hints" to listOf(
                        "POST /mcp/chat with { \"message\": \"list sheets\" } for simple English commands",
                        "Use /tool abc_* for direct protocol"
                    )
                )
                Response(Status.OK)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(mapper.writeValueAsString(info))
            }
            Method.POST -> {
                val body = request.bodyString()
                val reply = handleMessage(body)
                if (reply == null) {
                    // notifications → 202 / empty
                    Response(Status.ACCEPTED)
                        .header("Content-Type", "application/json; charset=utf-8")
                        .body("")
                } else {
                    Response(Status.OK)
                        .header("Content-Type", "application/json; charset=utf-8")
                        .body(reply)
                }
            }
            else -> Response(Status.METHOD_NOT_ALLOWED)
                .header("Allow", "GET, POST")
                .body("""{"error":"Use GET (info) or POST (JSON-RPC)"}""")
        }
    }

    /**
     * Blocking stdio loop. Intended for MCP hosts (Claude Desktop, Cursor, etc.).
     * Does not start the HTTP server by itself — caller decides process lifecycle.
     */
    fun serveStdio(
        input: BufferedReader = BufferedReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8)),
        output: PrintWriter = PrintWriter(System.out, true, StandardCharsets.UTF_8)
    ) {
        Trace.logInfo("MCP stdio transport ready (read-only abc_* tools)")
        while (true) {
            val line = input.readLine() ?: break
            if (line.isBlank()) continue
            val reply = handleMessage(line)
            if (reply != null) {
                output.println(reply)
                output.flush()
            }
        }
    }

    fun handleMessage(raw: String): String? {
        val root: JsonNode = try {
            mapper.readTree(raw)
        } catch (ex: Exception) {
            return rpcError(null, -32700, "Parse error: ${ex.message}")
        }

        // Batch not required for v1
        if (!root.isObject) {
            return rpcError(null, -32600, "Invalid Request: expected JSON object")
        }

        val method = root.path("method").asText(null)
        val idNode = root.get("id")
        val isNotification = idNode == null || idNode.isNull

        if (method.isNullOrBlank()) {
            return if (isNotification) null
            else rpcError(idNode, -32600, "Invalid Request: missing method")
        }

        // Notifications: no response
        if (method.startsWith("notifications/")) {
            return null
        }

        return try {
            when (method) {
                "initialize" -> rpcResult(idNode, initializeResult())
                "ping" -> rpcResult(idNode, emptyMap<String, Any?>())
                "tools/list" -> rpcResult(idNode, mapOf("tools" to tools.catalog().map { toolToMap(it) }))
                "tools/call" -> {
                    val params = root.get("params")
                    val name = params?.path("name")?.asText()
                    if (name.isNullOrBlank()) {
                        rpcError(idNode, -32602, "Invalid params: missing tool name")
                    } else {
                        val argsNode = params.get("arguments")
                        val args: Map<String, Any?>? = when {
                            argsNode == null || argsNode.isNull -> emptyMap()
                            argsNode.isObject -> mapper.convertValue(
                                argsNode,
                                mapper.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java)
                            )
                            else -> emptyMap()
                        }
                        val result = tools.call(name, args)
                        rpcResult(
                            idNode,
                            mapOf(
                                "content" to listOf(
                                    mapOf("type" to "text", "text" to result.text)
                                ),
                                "isError" to result.isError
                            )
                        )
                    }
                }
                "resources/list" -> rpcResult(idNode, mapOf("resources" to emptyList<Any>()))
                "prompts/list" -> rpcResult(idNode, mapOf("prompts" to emptyList<Any>()))
                else -> rpcError(idNode, -32601, "Method not found: $method")
            }
        } catch (ex: Exception) {
            rpcError(idNode, -32603, "Internal error: ${ex.message}")
        }
    }

    private fun initializeResult(): Map<String, Any?> = mapOf(
        "protocolVersion" to protocolVersion,
        "capabilities" to mapOf(
            "tools" to mapOf("listChanged" to false)
        ),
        "serverInfo" to mapOf(
            "name" to "onmind-xdb-mcp",
            "version" to onmindxdb.version
        ),
        "instructions" to
            "Read-only ABC access to OnMind-XDB. Use abc_list / abc_describe to learn sheets, " +
                "then abc_find to query. Writes are not available via MCP in this version."
    )

    private fun toolToMap(tool: AbcMcpTools.ToolDef): Map<String, Any?> = mapOf(
        "name" to tool.name,
        "description" to tool.description,
        "inputSchema" to tool.inputSchema
    )

    private fun rpcResult(id: JsonNode?, result: Any?): String {
        val node = mapper.createObjectNode()
        node.put("jsonrpc", "2.0")
        putId(node, id)
        node.set<JsonNode>("result", mapper.valueToTree(result))
        return mapper.writeValueAsString(node)
    }

    private fun rpcError(id: JsonNode?, code: Int, message: String): String {
        val node = mapper.createObjectNode()
        node.put("jsonrpc", "2.0")
        putId(node, id)
        val err = node.putObject("error")
        err.put("code", code)
        err.put("message", message)
        return mapper.writeValueAsString(node)
    }

    private fun putId(node: ObjectNode, id: JsonNode?) {
        when {
            id == null || id.isNull -> node.putNull("id")
            id.isInt -> node.put("id", id.asInt())
            id.isLong -> node.put("id", id.asLong())
            id.isNumber -> node.put("id", id.asDouble())
            id.isTextual -> node.put("id", id.asText())
            else -> node.putNull("id")
        }
    }
}
