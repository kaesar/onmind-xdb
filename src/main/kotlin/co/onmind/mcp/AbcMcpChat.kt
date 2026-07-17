package co.onmind.mcp

import co.onmind.util.JsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Lightweight dashboard chat bridge over [AbcMcpTools].
 *
 * No external LLM dependency (keeps the fat JAR small). Interprets short natural-language /
 * slash commands and maps them to abc_* tools. A future iteration can swap the planner for a real model.
 */
class AbcMcpChat(
    private val tools: AbcMcpTools
) {
    private val mapper = JsonMapper.instance

    fun httpHandler(): HttpHandler = { request: Request ->
        when (request.method) {
            Method.GET -> {
                val info = mapOf(
                    "ok" to true,
                    "service" to "OnMind-XDB MCP Chat",
                    "mode" to tools.modeLabel(),
                    "write" to tools.writeEnabled,
                    "tools" to tools.catalog().map {
                        mapOf("name" to it.name, "write" to it.write, "description" to it.description)
                    },
                    "hints" to listOf(
                        "status",
                        "list sheets",
                        "describe PRODUCTS",
                        "find PRODUCTS where any03 = 'x' size 10",
                        "/tool abc_find {\"some\":\"PRODUCTS.SHEET\",\"size\":\"5\"}",
                        if (tools.writeEnabled) "create sheet demo title \"Demo\"" else "(enable mcp.write=+ for create/define)"
                    )
                )
                json(Status.OK, info)
            }
            Method.POST -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val body = mapper.readValue(request.bodyString(), Map::class.java) as Map<String, Any?>
                    val message = body["message"]?.toString()?.trim().orEmpty()
                    if (message.isEmpty()) {
                        json(Status.BAD_REQUEST, mapOf("ok" to false, "error" to "message is required"))
                    } else {
                        val reply = turn(message)
                        json(Status.OK, reply)
                    }
                } catch (ex: Exception) {
                    json(Status.BAD_REQUEST, mapOf("ok" to false, "error" to (ex.message ?: "bad request")))
                }
            }
            else -> Response(Status.METHOD_NOT_ALLOWED)
                .header("Allow", "GET, POST")
                .body("""{"error":"Use GET or POST"}""")
        }
    }

    fun turn(message: String): Map<String, Any?> {
        val plan = plan(message)
        if (plan == null) {
            return mapOf(
                "ok" to true,
                "role" to "assistant",
                "content" to helpText(),
                "tool" to null,
                "toolResult" to null
            )
        }

        val result = tools.call(plan.name, plan.arguments)
        val summary = if (result.isError) {
            "Tool `${plan.name}` failed."
        } else {
            "Tool `${plan.name}` completed."
        }

        return mapOf(
            "ok" to !result.isError,
            "role" to "assistant",
            "content" to summary,
            "tool" to mapOf(
                "name" to plan.name,
                "arguments" to plan.arguments
            ),
            "toolResult" to mapOf(
                "isError" to result.isError,
                "text" to result.text
            )
        )
    }

    private data class Plan(val name: String, val arguments: Map<String, Any?>)

    private fun plan(raw: String): Plan? {
        val message = raw.trim()
        if (message.isEmpty()) return null

        // Power user: /tool name {json}
        val toolSlash = Regex("""^/tool\s+(\w+)\s*(\{[\s\S]*\})?\s*$""", RegexOption.IGNORE_CASE)
        toolSlash.matchEntire(message)?.let { m ->
            val name = m.groupValues[1]
            val jsonArgs = m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
            val args: Map<String, Any?> = if (jsonArgs != null) {
                @Suppress("UNCHECKED_CAST")
                mapper.readValue(jsonArgs, Map::class.java) as Map<String, Any?>
            } else emptyMap()
            return Plan(name, args)
        }

        val lower = message.lowercase()

        if (lower in setOf("help", "?", "ayuda", "comandos")) return null

        if (lower.matches(Regex("""^(status|estado|version|ping)\b.*"""))) {
            return Plan("abc_status", emptyMap())
        }

        if (lower.matches(Regex("""^(list|listar|sheets|hojas|collections)\b.*"""))) {
            val scheme = Regex("""\b(scheme|esquema)\s*[:=]?\s*(\w+)""", RegexOption.IGNORE_CASE)
                .find(message)?.groupValues?.getOrNull(2)
            return Plan("abc_list", scheme?.let { mapOf("scheme" to it) } ?: emptyMap())
        }

        Regex("""^(describe|describir|desc)\s+([A-Za-z0-9_.]+)""", RegexOption.IGNORE_CASE)
            .find(message)?.let { m ->
                return Plan("abc_describe", mapOf("some" to m.groupValues[2]))
            }

        // find SHEET [where FILTER] [size N]
        Regex(
            """^(find|buscar|query)\s+([A-Za-z0-9_.]+)(?:\s+(?:where|con|with)\s+(.+?))?(?:\s+size\s+(\d+))?\s*$""",
            RegexOption.IGNORE_CASE
        ).find(message)?.let { m ->
            val args = mutableMapOf<String, Any?>(
                "some" to m.groupValues[2]
            )
            m.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { args["with"] = it.trim() }
            m.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { args["size"] = it }
            return Plan("abc_find", args)
        }

        // create sheet NAME [title "..."]
        Regex(
            """^(?:create|crear)(?:\s+sheet)?\s+([A-Za-z0-9_]+)(?:\s+title\s+["'](.+?)["'])?\s*$""",
            RegexOption.IGNORE_CASE
        ).find(message)?.let { m ->
            val args = mutableMapOf<String, Any?>("some" to m.groupValues[1])
            m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { args["title"] = it }
            return Plan("abc_create", args)
        }

        // define SHEET spec ...
        Regex(
            """^(?:define|definir)\s+([A-Za-z0-9_.]+)\s+(?:spec\s+)?(.+)$""",
            RegexOption.IGNORE_CASE
        ).find(message)?.let { m ->
            return Plan(
                "abc_define",
                mapOf(
                    "some" to m.groupValues[1],
                    "spec" to m.groupValues[2].trim().trim('"', '\'')
                )
            )
        }

        // schema SHEET [title "..."] [spec ...]
        Regex(
            """^(?:schema|esquema)\s+([A-Za-z0-9_]+)(?:\s+title\s+["'](.+?)["'])?(?:\s+spec\s+(.+))?\s*$""",
            RegexOption.IGNORE_CASE
        ).find(message)?.let { m ->
            val args = mutableMapOf<String, Any?>("some" to m.groupValues[1])
            m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { args["title"] = it }
            m.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let {
                args["spec"] = it.trim().trim('"', '\'')
            }
            return Plan("abc_schema", args)
        }

        return null
    }

    private fun helpText(): String {
        val writeHint = if (tools.writeEnabled) {
            """
            |Write (mcp.write=+):
            |- create sheet NAME title "Title"
            |- define NAME spec any02=code,any03=name
            |- schema NAME title "Title" spec any02=code,any03=name
            """.trimMargin()
        } else {
            "Write tools disabled (set mcp.write=+ for create/define/schema)."
        }

        return """
            |I map short commands to ABC MCP tools (no external LLM in this sketch).
            |
            |Read:
            |- status
            |- list sheets
            |- describe PRODUCTS
            |- find PRODUCTS where any03 = 'x' size 10
            |- /tool abc_find {"some":"PRODUCTS.SHEET","size":"5"}
            |
            |$writeHint
            |
            |Mode: ${tools.modeLabel()}
            |Tools: ${tools.catalog().joinToString { it.name }}
        """.trimMargin()
    }

    private fun json(status: Status, body: Any): Response =
        Response(status)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))
}
