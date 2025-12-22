package co.onmind.app

import co.onmind.db.RDB
import co.onmind.util.JsonMapper
import co.onmind.util.Rote
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import onmindxdb
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path as PathLens
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.nio.file.Path
import java.nio.file.Files

fun Request.authUser(): String = this.header("X-Auth-User") ?: "anonymous"

class AppUI {
    private val xdb = RDB()
    private val templateEngine: TemplateEngine = createTemplateEngine()
    private val json = JsonMapper.instance

    private fun createTemplateEngine(): TemplateEngine {
        val devPath = Path.of("src/main/resources/kte")
        return if (Files.exists(devPath)) {
            TemplateEngine.create(DirectoryCodeResolver(devPath), ContentType.Html)
        } else {
            TemplateEngine.createPrecompiled(ContentType.Html)
        }
    }

    fun routes() = routes(
        "/app" bind Method.GET to { _: Request -> dashboard() },
        "/app/" bind Method.GET to { _: Request -> dashboard() },
        "/app/data" bind Method.GET to { _: Request -> dataList() },
        "/app/data/{sheet}" bind Method.GET to { req: Request -> dataView(req) },
        "/app/users" bind Method.GET to { _: Request -> usersList() },
        "/app/settings" bind Method.GET to { _: Request -> settingsList() },
        "/app/sheets" bind Method.GET to { _: Request -> sheetsList() }
    )

    private fun dashboard(): Response {
        if (!onmindxdb.uiEnabled) {
            return Response(Status.OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
        }
        val output = renderTemplate("dashboard", mapOf("title" to "Dashboard"))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun dataList(): Response {
        if (!onmindxdb.uiEnabled) {
            return Response(Status.OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
        }
        val limit = onmindxdb.queryLimit
        val query = "SELECT id, kit01 as code, kit02 as name, kit03 as title FROM xykit WHERE kitxy = 'SHEET' AND kit01 LIKE '%.SHEET' LIMIT $limit"
        val sheets = xdb.forQuery(query) ?: emptyList()
        val columns = listOf(
            mapOf("key" to "code", "header" to "Code"),
            mapOf("key" to "name", "header" to "Name"),
            mapOf("key" to "title", "header" to "Title")
        )
        val output = renderTemplate("data-list", mapOf(
            "sheets" to sheets,
            "sheetsJson" to json.writeValueAsString(sheets),
            "columnsJson" to json.writeValueAsString(columns)
        ))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun dataView(req: Request): Response {
        if (!onmindxdb.uiEnabled) {
            return Response(Status.OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
        }
        val sheetLens = PathLens.of("sheet")
        val sheet = sheetLens(req)
        val code = "${sheet.uppercase()}.SHEET"
        
        val kitQuery = "SELECT * FROM xykit WHERE kit01='$code'"
        val kitRows = xdb.forQuery(kitQuery)
        if (kitRows.isNullOrEmpty()) {
            return Response(Status.NOT_FOUND).body("Sheet not found")
        }
        
        val limit = onmindxdb.queryLimit
        val dataQuery = "SELECT * FROM xyany WHERE anyxy='$code' LIMIT $limit"
        val records = xdb.forQuery(dataQuery) ?: emptyList()
        val columns = listOf(
            mapOf("key" to "id", "header" to "ID"),
            mapOf("key" to "any01", "header" to "Code"),
            mapOf("key" to "any02", "header" to "Data")
        )
        
        val output = renderTemplate("data-view", mapOf(
            "sheet" to kitRows[0],
            "records" to records,
            "recordsJson" to json.writeValueAsString(records),
            "columnsJson" to json.writeValueAsString(columns),
            "code" to code
        ))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun usersList(): Response {
        if (!onmindxdb.uiEnabled) {
            return Response(Status.OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
        }
        val limit = onmindxdb.queryLimit
        val query = "SELECT * FROM xykey WHERE keyxy IN ('USER', 'ROLE') LIMIT $limit"
        val users = xdb.forQuery(query) ?: emptyList()
        val columns = listOf(
            mapOf("key" to "key01", "header" to "Code"),
            mapOf("key" to "key02", "header" to "Name"),
            mapOf("key" to "keyxy", "header" to "Type"),
            mapOf("key" to "key20", "header" to "Status")
        )
        val output = renderTemplate("users-list", mapOf(
            "users" to users,
            "usersJson" to json.writeValueAsString(users),
            "columnsJson" to json.writeValueAsString(columns)
        ))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun settingsList(): Response {
        if (!onmindxdb.uiEnabled) {
            return Response(Status.OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
        }
        val limit = onmindxdb.queryLimit
        val query = "SELECT * FROM xyset LIMIT $limit"
        val settings = xdb.forQuery(query) ?: emptyList()
        val columns = listOf(
            mapOf("key" to "set01", "header" to "Code"),
            mapOf("key" to "set02", "header" to "Name"),
            mapOf("key" to "set03", "header" to "Value")
        )
        val output = renderTemplate("settings-list", mapOf(
            "settings" to settings,
            "settingsJson" to json.writeValueAsString(settings),
            "columnsJson" to json.writeValueAsString(columns)
        ))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun sheetsList(): Response {
        if (!onmindxdb.uiEnabled) {
            return Response(Status.OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
        }
        val limit = onmindxdb.queryLimit
        val query = "SELECT * FROM xykit WHERE kitxy IN ('SHEET','SETUP') LIMIT $limit"
        val sheets = xdb.forQuery(query) ?: emptyList()
        val columns = listOf(
            mapOf("key" to "kit01", "header" to "Code"),
            mapOf("key" to "kit02", "header" to "Name"),
            mapOf("key" to "kit03", "header" to "Title"),
            mapOf("key" to "kit05", "header" to "Model"),
            mapOf("key" to "kitxy", "header" to "Scheme")
        )
        val output = renderTemplate("sheets-list", mapOf(
            "sheets" to sheets,
            "sheetsJson" to json.writeValueAsString(sheets),
            "columnsJson" to json.writeValueAsString(columns)
        ))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun renderTemplate(name: String, model: Map<String, Any>): String {
        return try {
            val output = StringOutput()
            templateEngine.render("$name.kte", model, output)
            output.toString()
        } catch (e: Exception) {
            try {
                val errorOutput = StringOutput()
                templateEngine.render("error.kte", mapOf(
                    "templateName" to name,
                    "errorMessage" to (e.message ?: "Unknown error"),
                    "stackTrace" to e.stackTraceToString(),
                    "version" to onmindxdb.version
                ), errorOutput)
                errorOutput.toString()
            } catch (errorTemplateException: Exception) {
                // Fallback si el template de error también falla
                "<html><body><h1>Critical Error</h1><p>Template: $name</p><pre>${e.message}</pre></body></html>"
            }
        }
    }
}
