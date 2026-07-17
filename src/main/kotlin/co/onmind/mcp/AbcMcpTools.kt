package co.onmind.mcp

import co.onmind.api.AbcAPI
import co.onmind.io.AbcBody
import co.onmind.util.JsonMapper
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status

/**
 * MCP tools over ABC (Articulable / Abstract Business Controller).
 *
 * Read tools are always available. Schema writes ([abc_create], [abc_define], [abc_schema])
 * require [writeEnabled] (config `mcp.write=+`). Row-level insert/update/delete stay out of scope.
 */
class AbcMcpTools(
    private val abc: AbcAPI = AbcAPI(),
    val writeEnabled: Boolean = false,
    private val defaultUser: String = "mcp"
) {
    private val mapper = JsonMapper.instance

    data class ToolDef(
        val name: String,
        val description: String,
        val inputSchema: Map<String, Any?>,
        val write: Boolean = false
    )

    fun modeLabel(): String = if (writeEnabled) "read+schema-write" else "read-only"

    fun catalog(): List<ToolDef> {
        val read = listOf(
            ToolDef(
                name = "abc_status",
                description = "Return OnMind-XDB service status (version, driver, embedded flag, MCP mode).",
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to emptyMap<String, Any>(),
                    "additionalProperties" to false
                )
            ),
            ToolDef(
                name = "abc_list",
                description = "List defined sheets/archetypes (xykit). Optional scheme defaults to SHEET.",
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "scheme" to mapOf(
                            "type" to "string",
                            "description" to "Kit scheme filter (maps to ABC 'with'). Default: SHEET"
                        )
                    ),
                    "additionalProperties" to false
                )
            ),
            ToolDef(
                name = "abc_describe",
                description = "Describe one sheet/collection: metadata and field spec (kit05) for a given 'some' code.",
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "some" to mapOf(
                            "type" to "string",
                            "description" to "Sheet/collection code, e.g. persons or PERSONS.SHEET"
                        ),
                        "scheme" to mapOf(
                            "type" to "string",
                            "description" to "Scheme if 'some' has no suffix. Default: SHEET"
                        )
                    ),
                    "required" to listOf("some"),
                    "additionalProperties" to false
                )
            ),
            ToolDef(
                name = "abc_find",
                description = "Query records via ABC find (read-only). Uses from/some/with/show/size like POST /abc.",
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "some" to mapOf(
                            "type" to "string",
                            "description" to "Collection/sheet code (required), e.g. persons or USER"
                        ),
                        "from" to mapOf(
                            "type" to "string",
                            "description" to "Entity table: xyany (default), xykey, xyset, xydoc, xykit"
                        ),
                        "with" to mapOf(
                            "type" to "string",
                            "description" to "SQL-like filter appended as AND, e.g. any03 = 'peter'"
                        ),
                        "show" to mapOf(
                            "type" to "string",
                            "description" to "Columns to return. Default: *"
                        ),
                        "size" to mapOf(
                            "type" to "string",
                            "description" to "Max rows (capped by server query limit). Default: 50"
                        )
                    ),
                    "required" to listOf("some"),
                    "additionalProperties" to false
                )
            )
        )

        if (!writeEnabled) return read

        val write = listOf(
            ToolDef(
                name = "abc_create",
                description = "Create a new sheet/archetype (ABC what=create). Requires mcp.write=+. Does not insert data rows.",
                write = true,
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "some" to mapOf(
                            "type" to "string",
                            "description" to "Internal code/name of the sheet, e.g. persons"
                        ),
                        "title" to mapOf(
                            "type" to "string",
                            "description" to "Human title (ABC 'show'). Defaults to uppercase some"
                        ),
                        "scheme" to mapOf(
                            "type" to "string",
                            "description" to "Scheme (ABC 'with'). Default: SHEET"
                        ),
                        "from" to mapOf(
                            "type" to "string",
                            "description" to "Entity table. Default: xyany"
                        ),
                        "hint" to mapOf("type" to "string", "description" to "Optional short hint"),
                        "spec" to mapOf(
                            "type" to "string",
                            "description" to "Optional initial field spec (same format as abc_define). Default: []"
                        ),
                        "user" to mapOf(
                            "type" to "string",
                            "description" to "Acting user (ABC 'user'). Default: mcp"
                        )
                    ),
                    "required" to listOf("some"),
                    "additionalProperties" to false
                )
            ),
            ToolDef(
                name = "abc_define",
                description = "Define/update field mapping for an existing sheet (ABC what=define, puts=spec). Requires mcp.write=+. Spec examples: 'any02=code,any03=name' plane form or '[]'.",
                write = true,
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "some" to mapOf(
                            "type" to "string",
                            "description" to "Sheet code, e.g. persons or PERSONS.SHEET"
                        ),
                        "spec" to mapOf(
                            "type" to "string",
                            "description" to "Field specification stored in kit05 (ABC 'puts')"
                        ),
                        "title" to mapOf(
                            "type" to "string",
                            "description" to "Optional new title (ABC 'show')"
                        ),
                        "scheme" to mapOf(
                            "type" to "string",
                            "description" to "Scheme if some has no suffix. Default: SHEET"
                        )
                    ),
                    "required" to listOf("some", "spec"),
                    "additionalProperties" to false
                )
            ),
            ToolDef(
                name = "abc_schema",
                description = "Combined create+define: ensure the sheet exists, then apply field spec. Preferred write path for agents. Requires mcp.write=+. " +
                    "If the sheet already exists, only define runs. If create fails for another reason, the error is returned.",
                write = true,
                inputSchema = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "some" to mapOf(
                            "type" to "string",
                            "description" to "Sheet code, e.g. persons"
                        ),
                        "title" to mapOf(
                            "type" to "string",
                            "description" to "Human title. Defaults to uppercase some"
                        ),
                        "spec" to mapOf(
                            "type" to "string",
                            "description" to "Field specification (ABC puts). Default: []"
                        ),
                        "scheme" to mapOf(
                            "type" to "string",
                            "description" to "Scheme. Default: SHEET"
                        ),
                        "from" to mapOf(
                            "type" to "string",
                            "description" to "Entity table. Default: xyany"
                        ),
                        "user" to mapOf(
                            "type" to "string",
                            "description" to "Acting user. Default: mcp"
                        )
                    ),
                    "required" to listOf("some"),
                    "additionalProperties" to false
                )
            )
        )

        return read + write
    }

    fun call(name: String, arguments: Map<String, Any?>?): ToolResult {
        return try {
            when (name) {
                "abc_status" -> ToolResult.ok(statusJson())
                "abc_list" -> invokeAbc(
                    AbcBody(
                        what = "list",
                        with = argString(arguments, "scheme") ?: "SHEET"
                    )
                )
                "abc_describe" -> describe(arguments)
                "abc_find" -> {
                    val some = argString(arguments, "some")
                        ?: return ToolResult.error("Missing required argument: some")
                    invokeAbc(
                        AbcBody(
                            what = "find",
                            from = argString(arguments, "from") ?: "xyany",
                            some = some,
                            with = argString(arguments, "with"),
                            show = argString(arguments, "show") ?: "*",
                            size = argString(arguments, "size") ?: "50"
                        )
                    )
                }
                "abc_create" -> {
                    denyUnlessWrite()?.let { return it }
                    createSheet(arguments)
                }
                "abc_define" -> {
                    denyUnlessWrite()?.let { return it }
                    defineSheet(arguments)
                }
                "abc_schema" -> {
                    denyUnlessWrite()?.let { return it }
                    schemaSheet(arguments)
                }
                else -> ToolResult.error("Unknown tool: $name. Available: ${catalog().joinToString { it.name }}")
            }
        } catch (ex: Exception) {
            ToolResult.error(ex.message ?: ex::class.java.simpleName)
        }
    }

    private fun denyUnlessWrite(): ToolResult? {
        if (writeEnabled) return null
        return ToolResult.error(
            "Write tools are disabled. Set mcp.write=+ in onmind.ini and restart. " +
                "Available write tools when enabled: abc_create, abc_define, abc_schema."
        )
    }

    private fun createSheet(arguments: Map<String, Any?>?): ToolResult {
        val some = argString(arguments, "some")
            ?: return ToolResult.error("Missing required argument: some")
        val bare = some.substringBefore(".").lowercase()
        return invokeAbc(
            AbcBody(
                what = "create",
                from = argString(arguments, "from") ?: "xyany",
                some = bare,
                with = argString(arguments, "scheme") ?: "SHEET",
                show = argString(arguments, "title") ?: bare.uppercase(),
                puts = argString(arguments, "spec") ?: "[]",
                hint = argString(arguments, "hint"),
                user = argString(arguments, "user") ?: defaultUser
            )
        )
    }

    private fun defineSheet(arguments: Map<String, Any?>?): ToolResult {
        val some = argString(arguments, "some")
            ?: return ToolResult.error("Missing required argument: some")
        val spec = argString(arguments, "spec")
            ?: return ToolResult.error("Missing required argument: spec")
        val bare = some.substringBefore(".")
        return invokeAbc(
            AbcBody(
                what = "define",
                some = bare,
                with = argString(arguments, "scheme") ?: "SHEET",
                puts = spec,
                show = argString(arguments, "title")
            )
        )
    }

    /**
     * Preferred agent write path: create if missing, then always define when spec provided.
     */
    private fun schemaSheet(arguments: Map<String, Any?>?): ToolResult {
        val some = argString(arguments, "some")
            ?: return ToolResult.error("Missing required argument: some")
        val bare = some.substringBefore(".")
        val scheme = argString(arguments, "scheme") ?: "SHEET"
        val code = "${bare.uppercase()}.$scheme"
        val title = argString(arguments, "title") ?: bare.uppercase()
        val spec = argString(arguments, "spec") ?: "[]"
        val from = argString(arguments, "from") ?: "xyany"
        val user = argString(arguments, "user") ?: defaultUser

        val exists = describe(
            mapOf("some" to bare, "scheme" to scheme)
        )
        val steps = mutableListOf<Map<String, Any?>>()

        if (exists.isError) {
            val created = invokeAbc(
                AbcBody(
                    what = "create",
                    from = from,
                    some = bare.lowercase(),
                    with = scheme,
                    show = title,
                    puts = "[]",
                    user = user
                )
            )
            steps.add(
                mapOf(
                    "step" to "create",
                    "ok" to !created.isError,
                    "result" to tryParseJson(created.text)
                )
            )
            if (created.isError && !created.text.contains("Already exists", ignoreCase = true)) {
                return ToolResult.error(
                    mapper.writeValueAsString(
                        mapOf("ok" to false, "code" to code, "steps" to steps)
                    )
                )
            }
        } else {
            steps.add(mapOf("step" to "create", "ok" to true, "skipped" to true, "reason" to "already exists"))
        }

        val defined = invokeAbc(
            AbcBody(
                what = "define",
                some = bare,
                with = scheme,
                puts = spec,
                show = title
            )
        )
        steps.add(
            mapOf(
                "step" to "define",
                "ok" to !defined.isError,
                "result" to tryParseJson(defined.text)
            )
        )

        val ok = steps.all { it["ok"] == true }
        val payload = mapOf(
            "ok" to ok,
            "code" to code,
            "title" to title,
            "spec" to spec,
            "steps" to steps
        )
        val text = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload)
        return if (ok) ToolResult.ok(text) else ToolResult.error(text)
    }

    private fun describe(arguments: Map<String, Any?>?): ToolResult {
        val raw = argString(arguments, "some")
            ?: return ToolResult.error("Missing required argument: some")
        val scheme = (argString(arguments, "scheme") ?: "SHEET").uppercase()
        val code = if (raw.contains(".")) raw.uppercase() else "${raw.uppercase()}.$scheme"

        val listed = invokeAbc(AbcBody(what = "list", with = scheme))
        if (listed.isError) return listed

        return try {
            @Suppress("UNCHECKED_CAST")
            val root = mapper.readValue(listed.text, Map::class.java) as Map<String, Any?>
            val data = root["data"] as? List<*> ?: emptyList<Any?>()
            val match = data.filterIsInstance<Map<*, *>>().firstOrNull { row ->
                row["code"]?.toString()?.equals(code, ignoreCase = true) == true ||
                    row["name"]?.toString()?.equals(raw, ignoreCase = true) == true ||
                    row["kit01"]?.toString()?.equals(code, ignoreCase = true) == true
            }
            if (match == null) {
                ToolResult.error("Sheet not found: $code")
            } else {
                ToolResult.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(match))
            }
        } catch (ex: Exception) {
            ToolResult.error("Failed to parse list for describe: ${ex.message}")
        }
    }

    private fun statusJson(): String {
        val payload = mapOf(
            "ok" to true,
            "status" to "200",
            "service" to "OnMind-XDB",
            "version" to onmindxdb.version,
            "driver" to onmindxdb.driver,
            "embedded" to co.onmind.util.Rote.embedded,
            "mcp" to mapOf(
                "tools" to catalog().map { it.name },
                "mode" to modeLabel(),
                "write" to writeEnabled
            )
        )
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload)
    }

    private fun invokeAbc(body: AbcBody): ToolResult {
        val json = mapper.writeValueAsString(body)
        val request = Request(Method.POST, "/abc")
            .header("Content-Type", "application/json")
            .header("X-Auth-User", body.user ?: defaultUser)
            .body(json)
        val response = abc.mainControl(request)
        val text = response.bodyString()
        val isError = response.status != Status.OK && response.status != Status.CREATED
        return if (isError) ToolResult.error(text) else ToolResult.ok(text)
    }

    private fun tryParseJson(text: String): Any {
        return try {
            mapper.readValue(text, Any::class.java)
        } catch (_: Exception) {
            text
        }
    }

    private fun argString(arguments: Map<String, Any?>?, key: String): String? {
        val value = arguments?.get(key) ?: return null
        val text = value.toString().trim()
        return text.ifEmpty { null }
    }

    data class ToolResult(
        val text: String,
        val isError: Boolean
    ) {
        companion object {
            fun ok(text: String) = ToolResult(text = text, isError = false)
            fun error(text: String) = ToolResult(text = text, isError = true)
        }
    }
}
