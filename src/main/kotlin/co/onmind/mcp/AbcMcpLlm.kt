package co.onmind.mcp

import co.onmind.util.JsonMapper
import co.onmind.util.Trace
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Optional local LLM bridge (Ollama-compatible) for [AbcMcpChat].
 *
 * Uses only JDK [HttpClient] + Jackson — no extra JAR weight.
 * Supports:
 *  1. Native Ollama `tool_calls` when the model emits them
 *  2. JSON tool protocol in content (works well with OmniCoder-style models), e.g.
 *     `{"tool":"abc_list","arguments":{"scheme":"SHEET"}}`
 */
class AbcMcpLlm(
    private val baseUrl: String,
    private val model: String,
    private val tools: AbcMcpTools,
    private val timeoutSec: Long = 120,
    private val maxSteps: Int = 6
) {
    private val mapper = JsonMapper.instance
    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    val enabled: Boolean get() = baseUrl.isNotBlank() && model.isNotBlank()

    fun info(): Map<String, Any?> = mapOf(
        "provider" to "ollama",
        "url" to baseUrl,
        "model" to model,
        "timeout_sec" to timeoutSec,
        "max_steps" to maxSteps
    )

    fun turn(userMessage: String): Map<String, Any?> {
        val catalog = tools.catalog()
        val messages = mutableListOf<Map<String, Any?>>(
            mapOf("role" to "system", "content" to systemPrompt(catalog)),
            mapOf("role" to "user", "content" to userMessage)
        )
        val toolTrace = mutableListOf<Map<String, Any?>>()
        var lastContent = ""

        repeat(maxSteps) { step ->
            val response = chat(messages, catalog)
            val message = response.path("message")
            lastContent = message.path("content").asText("").trim()

            val calls = extractToolCalls(message, lastContent)
            if (calls.isEmpty()) {
                return mapOf(
                    "ok" to true,
                    "role" to "assistant",
                    "content" to lastContent.ifBlank { "Done." },
                    "llm" to info(),
                    "steps" to step + 1,
                    "tools" to toolTrace,
                    "tool" to toolTrace.lastOrNull()?.get("tool"),
                    "toolResult" to toolTrace.lastOrNull()?.get("toolResult")
                )
            }

            // Append assistant message (prefer original shape for multi-turn)
            val assistantMsg = mutableMapOf<String, Any?>(
                "role" to "assistant",
                "content" to lastContent
            )
            val nativeCalls = message.get("tool_calls")
            if (nativeCalls != null && !nativeCalls.isNull && nativeCalls.isArray && nativeCalls.size() > 0) {
                assistantMsg["tool_calls"] = mapper.convertValue(nativeCalls, Any::class.java)
            }
            messages.add(assistantMsg)

            for (call in calls) {
                val result = tools.call(call.name, call.arguments)
                val entry = mapOf(
                    "tool" to mapOf("name" to call.name, "arguments" to call.arguments),
                    "toolResult" to mapOf("isError" to result.isError, "text" to result.text)
                )
                toolTrace.add(entry)

                // Ollama-style tool message
                messages.add(
                    mapOf(
                        "role" to "tool",
                        "content" to result.text
                    )
                )
                // Also nudge models that only understand JSON protocol
                messages.add(
                    mapOf(
                        "role" to "user",
                        "content" to
                            "Tool `${call.name}` returned (isError=${result.isError}):\n${result.text}\n" +
                                "Continue. If you need another tool, emit JSON {\"tool\":\"...\",\"arguments\":{...}}. " +
                                "Otherwise answer the user in plain text without inventing data."
                    )
                )
            }
        }

        return mapOf(
            "ok" to true,
            "role" to "assistant",
            "content" to lastContent.ifBlank { "Reached max tool steps ($maxSteps). Partial results below." },
            "llm" to info(),
            "steps" to maxSteps,
            "tools" to toolTrace,
            "tool" to toolTrace.lastOrNull()?.get("tool"),
            "toolResult" to toolTrace.lastOrNull()?.get("toolResult")
        )
    }

    private fun systemPrompt(catalog: List<AbcMcpTools.ToolDef>): String {
        val toolLines = catalog.joinToString("\n") { t ->
            val write = if (t.write) " [WRITE]" else ""
            "- ${t.name}$write: ${t.description}\n  schema: ${mapper.writeValueAsString(t.inputSchema)}"
        }
        return """
            You are the OnMind-XDB assistant. You query a NoSQL database through ABC tools only.
            Never invent rows or sheets. Prefer tools over guessing.

            Mode: ${tools.modeLabel()}
            Write tools available: ${tools.writeEnabled}

            Available tools:
            $toolLines

            Tool calling protocol (required when you need data):
            Respond with a single JSON object (optionally fenced in ```json):
            {"tool":"<name>","arguments":{...}}

            After tool results are provided, either call another tool the same way or answer the user in clear prose.
            Do not expose internal IDs unless useful. Keep answers concise.
            For schema work use abc_schema when write is enabled; never invent insert/update/delete of rows.
        """.trimIndent()
    }

    private data class Call(val name: String, val arguments: Map<String, Any?>)

    private fun extractToolCalls(message: JsonNode, content: String): List<Call> {
        val out = mutableListOf<Call>()

        val native = message.get("tool_calls")
        if (native != null && native.isArray) {
            native.forEach { node ->
                val fn = node.path("function")
                val name = fn.path("name").asText(null)
                    ?: node.path("name").asText(null)
                if (!name.isNullOrBlank()) {
                    val argsNode = fn.get("arguments") ?: node.get("arguments")
                    out.add(Call(name, argsToMap(argsNode)))
                }
            }
        }
        if (out.isNotEmpty()) return out

        // Content JSON protocol
        val jsonBlob = extractJsonObject(content) ?: return emptyList()
        return try {
            val node = mapper.readTree(jsonBlob)
            val toolName = when {
                node.has("tool") -> node.path("tool").asText()
                node.has("name") -> node.path("name").asText()
                node.path("function").has("name") -> node.path("function").path("name").asText()
                else -> null
            }
            if (toolName.isNullOrBlank()) emptyList()
            else {
                val argsNode = when {
                    node.has("arguments") -> node.get("arguments")
                    node.has("args") -> node.get("args")
                    node.path("function").has("arguments") -> node.path("function").get("arguments")
                    else -> null
                }
                listOf(Call(toolName, argsToMap(argsNode)))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun argsToMap(node: JsonNode?): Map<String, Any?> {
        if (node == null || node.isNull) return emptyMap()
        return try {
            if (node.isTextual) {
                val text = node.asText().trim()
                if (text.isEmpty() || text == "{}") emptyMap()
                else {
                    @Suppress("UNCHECKED_CAST")
                    mapper.readValue(text, Map::class.java) as Map<String, Any?>
                }
            } else if (node.isObject) {
                @Suppress("UNCHECKED_CAST")
                mapper.convertValue(node, Map::class.java) as Map<String, Any?>
            } else emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun extractJsonObject(content: String): String? {
        if (content.isBlank()) return null
        val fenced = Regex("""```(?:json)?\s*(\{[\s\S]*?\})\s*```""", RegexOption.IGNORE_CASE)
            .find(content)?.groupValues?.getOrNull(1)
        if (fenced != null) return fenced.trim()

        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start >= 0 && end > start) return content.substring(start, end + 1)
        return null
    }

    private fun chat(messages: List<Map<String, Any?>>, catalog: List<AbcMcpTools.ToolDef>): JsonNode {
        val ollamaTools = catalog.map { t ->
            mapOf(
                "type" to "function",
                "function" to mapOf(
                    "name" to t.name,
                    "description" to t.description,
                    "parameters" to t.inputSchema
                )
            )
        }
        val body = mapOf(
            "model" to model,
            "stream" to false,
            "options" to mapOf("temperature" to 0.2),
            "messages" to messages,
            "tools" to ollamaTools
        )
        val payload = mapper.writeValueAsString(body)
        val url = baseUrl.trimEnd('/') + "/api/chat"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSec))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()

        val response = try {
            http.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (ex: Exception) {
            Trace.logWarn("MCP LLM request failed: ${ex.message}")
            throw IllegalStateException(
                "Cannot reach Ollama at $baseUrl (${ex.message}). " +
                    "Is ollama running? Try: ollama serve && ollama run $model"
            )
        }

        if (response.statusCode() !in 200..299) {
            throw IllegalStateException(
                "Ollama HTTP ${response.statusCode()}: ${response.body().take(400)}"
            )
        }
        return mapper.readTree(response.body())
    }

    companion object {
        fun fromConfig(
            tools: AbcMcpTools,
            llm: String?,
            url: String?,
            model: String?,
            timeoutSec: Long = 120
        ): AbcMcpLlm? {
            val provider = llm?.trim()?.lowercase().orEmpty()
            if (provider.isEmpty() || provider == "-" || provider == "off" || provider == "none") {
                return null
            }
            // Currently only ollama (OpenAI-compatible chat path can be added later)
            if (provider != "ollama" && provider != "+") return null
            val base = url?.trim().orEmpty().ifEmpty { "http://127.0.0.1:11434" }
            val mdl = model?.trim().orEmpty().ifEmpty { "carstenuhlig/omnicoder-9b:latest" }
            return AbcMcpLlm(baseUrl = base, model = mdl, tools = tools, timeoutSec = timeoutSec)
        }
    }
}
