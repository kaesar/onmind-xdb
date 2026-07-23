package co.onmind.mcp

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Lightweight MCP protocol checks (no JUnit — same style as TraceStructuredTest).
 * Run: ./gradlew -q testClasses && kotlin -cp ... or invoke main via gradle run if wired.
 */
object AbcMcpServerTest {

    private val mapper = jacksonObjectMapper()
    private val server = AbcMcpServer()

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== OnMind-XDB MCP Server Test ===")
        try {
            testInitialize()
            testToolsListReadOnly()
            testNotificationNoResponse()
            testUnknownMethod()
            testAbcStatusTool()
            println("All MCP tests passed!")
        } catch (e: Exception) {
            println("\nMCP test failed: ${e.message}")
            e.printStackTrace()
            kotlin.system.exitProcess(1)
        }
    }

    private fun testInitialize() {
        println("\n--- initialize ---")
        val raw =
            """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"0"}}}"""
        val reply = server.handleMessage(raw) ?: error("expected reply")
        val tree = mapper.readTree(reply)
        check(tree.path("result").path("serverInfo").path("name").asText() == "onmind-xdb-mcp")
        check(tree.path("result").path("capabilities").path("tools").isObject)
        println("ok")
    }

    private fun testToolsListReadOnly() {
        println("\n--- tools/list ---")
        val reply = server.handleMessage("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")!!
        val names = mapper.readTree(reply).path("result").path("tools").map { it.path("name").asText() }
        val expected = setOf("abc_status", "abc_list", "abc_describe", "abc_find", "abc_explain")
        val actual = names.toSet()
        check(actual == expected) {
            "unexpected tools: $names, expected: $expected"
        }
        check(names.none { it.contains("insert") || it.contains("update") || it.contains("delete") || it.contains("define") || it.contains("drop") }) {
            "unexpected write tools present: $names"
        }
        println("ok: $names")
    }

    private fun testNotificationNoResponse() {
        println("\n--- notifications/initialized ---")
        val reply = server.handleMessage("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")
        check(reply == null)
        println("ok")
    }

    private fun testUnknownMethod() {
        println("\n--- unknown method ---")
        val reply = server.handleMessage("""{"jsonrpc":"2.0","id":9,"method":"foo/bar"}""")!!
        check(mapper.readTree(reply).path("error").path("code").asInt() == -32601)
        println("ok")
    }

    private fun testAbcStatusTool() {
        println("\n--- tools/call abc_status ---")
        val reply =
            server.handleMessage(
                """{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"abc_status","arguments":{}}}"""
            )!!
        val tree = mapper.readTree(reply)
        check(!tree.path("result").path("isError").asBoolean())
        val text = tree.path("result").path("content").path(0).path("text").asText()
        check(text.contains("OnMind-XDB") || text.contains("ok")) { text }
        println("ok")
    }
}
