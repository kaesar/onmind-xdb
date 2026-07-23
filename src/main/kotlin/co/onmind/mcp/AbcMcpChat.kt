package co.onmind.mcp

import co.onmind.util.JsonMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Dashboard chat bridge over [AbcMcpTools].
 *
 * Modes:
 *  - **rules** (default): short natural-language / slash commands, no external model
 *  - **llm**: optional Ollama model (e.g. carstenuhlig/omnicoder-9b:latest) via [AbcMcpLlm]
 *
 * Power-user `/tool name {...}` always bypasses the planner/LLM and calls tools directly.
 */
class AbcMcpChat(
    private val tools: AbcMcpTools,
    private val llm: AbcMcpLlm? = null
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
                    "planner" to if (llm != null) "llm" else "rules",
                    "llm" to (llm?.info() ?: mapOf("enabled" to false)),
                    "tools" to tools.catalog().map {
                        mapOf("name" to it.name, "write" to it.write, "description" to it.description)
                    },
                    "hints" to listOf(
                        "status",
                        "list sheets",
                        "describe PRODUCTS",
                        "find PRODUCTS where any03 = 'x' size 10",
                        "/tool abc_find {\"some\":\"PRODUCTS.SHEET\",\"size\":\"5\"}",
                        if (tools.writeEnabled) "create sheet demo title \"Demo\"" else "(enable mcp.write=+ for create/define)",
                        if (llm != null) "LLM: ${llm.info()["model"]}" else "LLM off — set mcp.llm=ollama in onmind.ini"
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
                        val preferRules = body["rules"] == true || body["rules"] == "+"
                        val reply = turn(message, preferRules = preferRules)
                        json(Status.OK, reply)
                    }
                } catch (ex: Exception) {
                    json(
                        Status.BAD_REQUEST,
                        mapOf("ok" to false, "error" to (ex.message ?: "bad request"))
                    )
                }
            }
            else -> Response(Status.METHOD_NOT_ALLOWED)
                .header("Allow", "GET, POST")
                .body("""{"error":"Use GET or POST"}""")
        }
    }

    fun turn(message: String, preferRules: Boolean = false): Map<String, Any?> {
        // Power user always direct
        planSlashTool(message)?.let { plan ->
            val result = tools.call(plan.name, plan.arguments)
            return mapOf(
                "ok" to !result.isError,
                "role" to "assistant",
                "content" to if (result.isError) "Tool `${plan.name}` failed." else "Tool `${plan.name}` completed.",
                "planner" to "slash",
                "tool" to mapOf("name" to plan.name, "arguments" to plan.arguments),
                "toolResult" to mapOf("isError" to result.isError, "text" to result.text)
            )
        }

        if (!preferRules && llm != null) {
            return try {
                llm.turn(message) + mapOf("planner" to "llm")
            } catch (ex: Exception) {
                // Fall back to rules if LLM is down
                val fallback = rulesTurn(message)
                fallback + mapOf(
                    "planner" to "rules",
                    "llmError" to (ex.message ?: "llm failed")
                )
            }
        }

        return rulesTurn(message) + mapOf("planner" to "rules")
    }

    private fun rulesTurn(message: String): Map<String, Any?> {
        val plan = planRules(message)
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

    private fun planSlashTool(raw: String): Plan? {
        val toolSlash = Regex("""^/tool\s+(\w+)\s*(\{[\s\S]*\})?\s*$""", RegexOption.IGNORE_CASE)
        val m = toolSlash.matchEntire(raw.trim()) ?: return null
        val name = m.groupValues[1]
        val jsonArgs = m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
        val args: Map<String, Any?> = if (jsonArgs != null) {
            @Suppress("UNCHECKED_CAST")
            mapper.readValue(jsonArgs, Map::class.java) as Map<String, Any?>
        } else emptyMap()
        return Plan(name, args)
    }

    private fun planRules(raw: String): Plan? {
        val message = raw.trim()
        if (message.isEmpty()) return null

        val lower = message.lowercase()

        if (lower in setOf("help", "?", "ayuda", "comandos", "que puedes hacer", "que haces", "commands")) return null

        // Pattern table: (regex, planBuilder(MatchResult) -> Plan?)
        // Order matters — first match wins
        val patterns = listOf<Pair<Regex, (MatchResult) -> Plan?>>(
            // status / estado / ping
            Regex("""^(status|estado|version|ping|que tal|como estas|hello|hola)\b.*""") to
                { _: MatchResult -> Plan("abc_status", emptyMap()) },

            // list sheets / listar hojas / "puedes listarme los sheets"
            Regex("""^(list|listar|sheets|hojas|collections|dame|muestra|muestrame|puedes)\b.*(?:list|sheet|hoj|collection|tabla|coleccion).*""") to
                { m: MatchResult ->
                    val scheme = Regex("""\b(scheme|esquema)\s*[:=]?\s*(\w+)""", RegexOption.IGNORE_CASE)
                        .find(m.groupValues[0])?.groupValues?.getOrNull(2)
                    Plan("abc_list", scheme?.let { mapOf("scheme" to it) } ?: emptyMap())
                },
            // also catch single-word "list"
            Regex("""^(list|sheets|hojas|collections|listar)\b""") to
                { _: MatchResult -> Plan("abc_list", emptyMap()) },

            // describe X / que es X / informacion de X
            Regex("""^(describe|describir|desc|que es|que hay|informacion de|que contiene)\s+([A-Za-z0-9_.]+)""") to
                { m: MatchResult -> Plan("abc_describe", mapOf("some" to m.groupValues[2])) },

            // find / buscar / query / consultar / dame los / muestrame los
            Regex(
                """^(find|buscar|query|consultar|buscar en|dame los|muestra los|muestrame los)\s+([A-Za-z0-9_.]+)(?:\s+(?:where|con|with|donde|filtro)\s+(.+?))?(?:\s+size\s+(\d+))?\s*$"""
            ) to { m: MatchResult ->
                val args = mutableMapOf<String, Any?>("some" to m.groupValues[2])
                m.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { args["with"] = it.trim() }
                m.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { args["size"] = it }
                Plan("abc_find", args)
            },

            // create / crear / nueva sheet
            Regex(
                """^(?:create|crear|crea|nueva?)\s+(?:sheet|hoja|tabla|coleccion)?\s*([A-Za-z0-9_]+)(?:\s+title\s+["'](.+?)["'])?\s*$"""
            ) to { m: MatchResult ->
                val args = mutableMapOf<String, Any?>("some" to m.groupValues[1])
                m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { args["title"] = it }
                Plan("abc_create", args)
            },

            // define / definir
            Regex("""^(?:define|definir)\s+([A-Za-z0-9_.]+)\s+(?:spec\s+)?(.+)$""") to { m: MatchResult ->
                Plan("abc_define", mapOf("some" to m.groupValues[1], "spec" to m.groupValues[2].trim().trim('"', '\'')))
            },

            // schema / esquema (combinado create+define)
            Regex("""^(?:schema|esquema)\s+([A-Za-z0-9_]+)(?:\s+title\s+["'](.+?)["'])?(?:\s+spec\s+(.+))?\s*$""") to { m: MatchResult ->
                val args = mutableMapOf<String, Any?>("some" to m.groupValues[1])
                m.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { args["title"] = it }
                m.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { args["spec"] = it.trim().trim('"', '\'') }
                Plan("abc_schema", args)
            },

            // explain / explicar / como / how to / show query / show body
            Regex("""^(?:explain|explicar|como|how\s+to|show\s+(?:query|body))\s+(?:for\s+)?(find|list|describe|create|define|schema)\b(.*)$""") to { m: MatchResult ->
                val action = m.groupValues[1]
                val rest = m.groupValues[2].trim()
                val args = mutableMapOf<String, Any?>("action" to action)
                // Try to parse common params from the rest
                Regex("""\b(with|con|donde|filtro|where)\s+(.+?)(?:\s+size|$)""").find(rest)?.groupValues?.getOrNull(2)?.let { args["with"] = it.trim() }
                Regex("""\bsize\s+(\d+)""").find(rest)?.groupValues?.getOrNull(1)?.let { args["size"] = it }
                Regex("""\btitle\s+["'](.+?)["']""").find(rest)?.groupValues?.getOrNull(1)?.let { args["title"] = it }
                Regex("""\bspec\s+(.+)$""").find(rest)?.groupValues?.getOrNull(1)?.let { args["spec"] = it.trim() }
                Regex("""\bscheme\s+(\w+)""").find(rest)?.groupValues?.getOrNull(1)?.let { args["with"] = it }
                // extract sheet name (first word after action that looks like a sheet name)
                // For create, skip "sheet" keyword
                val sheetNameRegex = if (action == "create") """(?i)\bsheet\s+([A-Za-z][A-Za-z0-9_]*)\b""" else """\b([A-Za-z][A-Za-z0-9_]*)\b"""
                Regex(sheetNameRegex).find(rest)?.groupValues?.getOrNull(1)?.let { args["some"] = it }
                Plan("abc_explain", args)
            }
        )

        // First match wins
        return patterns.firstOrNull { (re, _) -> re.containsMatchIn(lower) }
            ?.let { (re, builder) -> re.find(lower)?.let { builder(it) } }
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

        val llmHint = if (llm != null) {
            "LLM planner: ${llm.info()["model"]} @ ${llm.info()["url"]}"
        } else {
            "LLM off. Set mcp.llm=ollama and mcp.llm.model=carstenuhlig/omnicoder-9b:latest"
        }

        return """
            |OnMind-XDB MCP chat.
            |
            |$llmHint
            |
            |Read commands (rules mode always works):
            |- status
            |- list sheets
            |- describe PRODUCTS
            |- find PRODUCTS where any03 = 'x' size 10
            |- explain find PRODUCTS where any03 = 'x' size 10
            |- explain create sheet PROYECTOS title "Proyectos" spec any02=code,any03=name
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
