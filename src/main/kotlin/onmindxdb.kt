import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.FOUND
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters.Cors
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.format.Jackson
//import org.http4k.cloudnative.env.Environment
//import org.http4k.client.ApacheClient
import java.sql.Connection
import java.util.Properties
import co.onmind.util.Rote
import co.onmind.api.AbcAPI
import co.onmind.app.AppUI
import co.onmind.db.RDB
import org.http4k.core.Status

fun swaggerUI(): String {
    val yamlContent = onmindxdb::class.java.getResourceAsStream("/swagger.yml")?.bufferedReader()?.readText() ?: ""
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>OnMind-XDB API Documentation</title>
        <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css" />
    </head>
    <body>
        <div id="swagger-ui"></div>
        <script src="https://unpkg.com/js-yaml@4.1.0/dist/js-yaml.min.js"></script>
        <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js"></script>
        <script>
            window.onload = function() {
                const yamlText = document.getElementById('swagger-spec').textContent;
                const spec = jsyaml.load(yamlText);
                SwaggerUIBundle({
                    spec: spec,
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [
                        SwaggerUIBundle.presets.apis,
                        SwaggerUIBundle.SwaggerUIStandalonePreset
                    ]
                });
            };
        </script>
        <script id="swagger-spec" type="text/yaml">
$yamlContent
        </script>
    </body>
    </html>
    """.trimIndent()
}

object onmindxdb {
    val os = System.getProperty("os.name")
    var dbc: Connection? = null  // AgroalDataSource? = null
    var driver = "org.h2.Driver"
    var dbfile: String? = null
    var queryLimit = 1200
    var config: Properties? = null
    val version = "1.0.0"

    @JvmStatic
    fun main(args: Array<String>) {
        val filex = Rote.getConfigFile()
        val cfg = Rote.getConfig(filex)  //val resource = Class::class.java.getResource("/application.conf")
        config = cfg
        dbc = Rote.getDB(cfg)
        dbfile = cfg.getProperty("app.local") + "xy/xybox.xdb"
        if (Rote.os.contains("Windows"))
            dbfile = dbfile!!.replace("/", "\\")

        val port = Rote.port
        val abc = AbcAPI()
        val appUI = AppUI()
        val xdb = RDB()
        xdb.readPoint()

        val appMode = cfg.getProperty("app.mode", "production")
        val enableSwagger = appMode != "production"
        
        print("Exposing api/db service ... ")
        val routesList = mutableListOf(
            "/" bind Method.GET to { _: Request -> Response(OK).status(Status.FOUND).header("Location", "/_/") },
            "/abc" bind Method.POST to abc.useControl(),
            "/abc" bind Method.GET to { _: Request ->
                Response(OK).body("""{"ok":true,"status":"200","service":"OnMind-XDB","version":"${version}","driver":"${driver}","embedded":${Rote.embedded}}""")
                    .header("Content-Type", "application/json")
            },
            appUI.routes()
        )
        
        if (enableSwagger) {
            routesList.add("/swagger" bind Method.GET to { _: Request -> Response(OK).body(swaggerUI()).header("Content-Type", "text/html") })
        }
        
        val app = routes(*routesList.toTypedArray()).withFilter(Cors(CorsPolicy(
            OriginPolicy.AllowAll(),  // AnyOf(listOf("*"))
            listOf("Content-Type", "Cache-Control"),
            listOf(Method.POST, Method.GET)  // Method.values().toList()
        )))

        println("[  OK!  ] => http://127.0.0.1:${port}")
        val serve = app.asServer(SunHttp(port)).start()  // Netty is an alternative
        serve.block()
    }
}
