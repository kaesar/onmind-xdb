package co.onmind.mcp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

/**
 * Lightweight MCP protocol checks (JSON-RPC surface, read-only tools).
 */
class AbcMcpServerTest {

    private val mapper = jacksonObjectMapper()
    private val server = AbcMcpServer()

    @Test
    fun initialize() {
        val raw =
            """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"0"}}}"""
        val reply = server.handleMessage(raw) ?: error("expected reply")
        val tree = mapper.readTree(reply)
        check(tree.path("result").path("serverInfo").path("name").asText() == "onmind-xdb-mcp")
        check(tree.path("result").path("capabilities").path("tools").isObject)
    }

    @Test
    fun toolsListReadOnly() {
        val reply = server.handleMessage("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")!!
        val names = mapper.readTree(reply).path("result").path("tools").map { it.path("name").asText() }
        val expected = setOf("abc_status", "abc_list", "abc_describe", "abc_find", "abc_explain")
        val actual = names.toSet()
        check(actual == expected) { "unexpected tools: $names, expected: $expected" }
        check(names.none { it.contains("insert") || it.contains("update") || it.contains("delete") || it.contains("define") || it.contains("drop") }) {
            "unexpected write tools present: $names"
        }
    }

    @Test
    fun notificationNoResponse() {
        val reply = server.handleMessage("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")
        check(reply == null)
    }

    @Test
    fun unknownMethod() {
        val reply = server.handleMessage("""{"jsonrpc":"2.0","id":9,"method":"foo/bar"}""")!!
        check(mapper.readTree(reply).path("error").path("code").asInt() == -32601)
    }

    @Test
    fun abcStatusTool() {
        val reply =
            server.handleMessage(
                """{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"abc_status","arguments":{}}}"""
            )!!
        val tree = mapper.readTree(reply)
        check(!tree.path("result").path("isError").asBoolean())
        val text = tree.path("result").path("content").path(0).path("text").asText()
        check(text.contains("OnMind-XDB") || text.contains("ok")) { text }
    }
}
