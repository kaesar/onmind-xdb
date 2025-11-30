package co.onmind.app

import co.onmind.db.RDB
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path as PathLens
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.nio.file.Path
import java.nio.file.Files

class AppUI {
    private val xdb = RDB()
    private val templateEngine: TemplateEngine = createTemplateEngine()

    private fun createTemplateEngine(): TemplateEngine {
        val devPath = Path.of("src/main/resources/jte")
        return if (Files.exists(devPath)) {
            TemplateEngine.create(DirectoryCodeResolver(devPath), ContentType.Html)
        } else {
            TemplateEngine.createPrecompiled(ContentType.Html)
        }
    }

    fun routes() = routes(
        "/_" bind Method.GET to { _: Request -> dashboard() },
        "/_/" bind Method.GET to { _: Request -> dashboard() },
        "/_/data" bind Method.GET to { req: Request -> dataList(req) },
        "/_/data/{sheet}" bind Method.GET to { req: Request -> dataView(req) },
        "/_/users" bind Method.GET to { _: Request -> usersList() },
        "/_/settings" bind Method.GET to { _: Request -> settingsList() },
        "/_/sheets" bind Method.GET to { _: Request -> sheetsList() }
    )

    private fun dashboard(): Response {
        val output = renderTemplate("dashboard", mapOf("title" to "Dashboard"))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun dataList(req: Request): Response {
        val query = "SELECT id, kit01 as code, kit02 as name, kit03 as title FROM xykit WHERE kitxy = 'SHEET' AND kit01 LIKE '%.SHEET'"
        val sheets = xdb.forQuery(query) ?: emptyList()
        val output = renderTemplate("data-list", mapOf("sheets" to sheets))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun dataView(req: Request): Response {
        val sheetLens = PathLens.of("sheet")
        val sheet = sheetLens(req)
        val code = "${sheet.uppercase()}.SHEET"
        
        val kitQuery = "SELECT * FROM xykit WHERE kit01='$code'"
        val kitRows = xdb.forQuery(kitQuery)
        if (kitRows.isNullOrEmpty()) {
            return Response(Status.NOT_FOUND).body("Sheet not found")
        }
        
        val dataQuery = "SELECT * FROM xyany WHERE anyxy='$code'"
        val records = xdb.forQuery(dataQuery) ?: emptyList()
        
        val output = renderTemplate("data-view", mapOf(
            "sheet" to kitRows[0],
            "records" to records,
            "code" to code
        ))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun usersList(): Response {
        val query = "SELECT * FROM xykey WHERE keyxy IN ('USER', 'ROLE')"
        val users = xdb.forQuery(query) ?: emptyList()
        val output = renderTemplate("users-list", mapOf("users" to users))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun settingsList(): Response {
        val query = "SELECT * FROM xyset"
        val settings = xdb.forQuery(query) ?: emptyList()
        val output = renderTemplate("settings-list", mapOf("settings" to settings))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun sheetsList(): Response {
        val query = "SELECT * FROM xykit WHERE kitxy = 'SHEET'"
        val sheets = xdb.forQuery(query) ?: emptyList()
        val output = renderTemplate("sheets-list", mapOf("sheets" to sheets))
        return Response(Status.OK).body(output).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun renderTemplate(name: String, model: Map<String, Any>): String {
        return try {
            val output = StringOutput()
            templateEngine.render("$name.jte", model, output)
            output.toString()
        } catch (e: Exception) {
            "<html><body><h1>Template Error</h1><pre>${e.message}</pre></body></html>"
        }
    }
}
