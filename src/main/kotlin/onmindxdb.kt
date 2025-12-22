import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters.Cors
import org.http4k.filter.ServerFilters.GZip
import org.http4k.filter.ServerFilters.RequestTracing
import org.http4k.filter.ResponseFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.server.Jetty
import org.http4k.server.asServer
import io.agroal.api.AgroalDataSource
import java.sql.Connection
import java.util.Properties
import co.onmind.util.CoherenceConfig
import co.onmind.util.CoherenceStore
import co.onmind.util.JsonMapper
import co.onmind.util.Rote
import co.onmind.util.Trace
import co.onmind.util.Swagger
import co.onmind.api.AbcAPI
import co.onmind.app.AppUI
import co.onmind.db.RDB
import co.onmind.auth.AuthConfig

object onmindxdb {
    val os = System.getProperty("os.name")
    var dataSource: AgroalDataSource? = null
    var dbc: Connection? = null
    var driver = "org.h2.Driver"
    var dbfile: String? = null
    var queryLimit = 1200
    var config: Properties? = null
    val version = "0.9.0"
    var uiEnabled = true
    private val json = JsonMapper.instance

    @JvmStatic
    fun main(args: Array<String>) {
        val filex = Rote.getConfigFile()
        val cfg = Rote.getConfig(filex)
        config = cfg
        dataSource = Rote.getDataSource(cfg)
        dbc = dataSource?.connection
        dbfile = cfg.getProperty("app.local") + "xy/xybox.xdb"
        if (Rote.os.contains("Windows"))
            dbfile = dbfile!!.replace("/", "\\")

        CoherenceConfig.init(cfg)

        val port = Rote.port
        val abc = AbcAPI()
        val appUI = AppUI()
        val xdb = RDB()

        val logLevel = CoherenceConfig.logLevel
        if (logLevel > 0) {
            Trace.init(cfg.getProperty("app.local") + "onmind-xdb.log", logLevel)
        }

        CoherenceStore.init(xdb)
        xdb.readPoint()

        val appMode = cfg.getProperty("app.mode", "production")
        val enableSwagger = appMode != "production"
        uiEnabled = cfg.getProperty("app.ui", "+") == "+"
        
        val authConfig = AuthConfig.fromConfig(cfg)
        val authProvider = authConfig.createProvider()
        val appLanguage = cfg.getProperty("app.language", "en")
        
        print("Exposing api/db service ... ")
        val routesList = mutableListOf(
            "/" bind Method.GET to handleRoot(),
            "/abc" bind Method.POST to abc.useControl(),
            "/abc" bind Method.GET to handleAbcStatus(),
            "/api/store/coherence" bind Method.GET to handleCoherenceStats(),
            "/api/store/coherence/verify" bind Method.POST to handleCoherenceVerify(),
            "/api/store/coherence/sync" bind Method.POST to handleCoherenceSync(),
            "/api/trace/stats" bind Method.GET to handleTraceStats(),
            "/health" bind Method.GET to handleHealth(),
            "/static" bind GZip().then(static(Classpath("/static"))),
            appUI.routes()
        )

        if (enableSwagger) {
            routesList.add("/swagger" bind Method.GET to { _: Request -> Response(OK).body(Swagger.ui()).header("Content-Type", "text/html") })
        }
        
        val app = RequestTracing()
            .then(ResponseFilters.ReportHttpTransaction { tx ->
                val logMsg = "[${tx.request.method}] ${tx.request.uri} -> ${tx.response.status.code}"
                // Usar el nuevo sistema de trace optimizado
                Trace.logRequest(tx.request.method.toString(), tx.request.uri.toString(), tx.response.status.code)
                
                // Fallback a println si trace está desactivado
                if (CoherenceConfig.logLevel == 0)
                    println(logMsg)
            })
            .then(authProvider.filter())
            .then(Cors(CorsPolicy(
                OriginPolicy.AllowAll(),
                listOf("Content-Type", "Cache-Control", "X-Request-Id"),
                listOf(Method.POST, Method.GET)
            )))
            .then(routes(*routesList.toTypedArray()))

        println("[  OK!  ] => http://127.0.0.1:${port}\n")
        val serve = app.asServer(Jetty(port)).start()
        
        Runtime.getRuntime().addShutdownHook(Thread {
            Trace.shutdown()
        })
        
        serve.block()
    }

    private fun handleRoot(): (Request) -> Response = { _: Request ->
        if (uiEnabled) Response(OK).status(Status.FOUND).header("Location", "/app/")
        else Response(OK).body(Rote.welcome()).header("Content-Type", "text/html; charset=utf-8")
    }

    private fun handleAbcStatus(): (Request) -> Response = { _: Request ->
        Response(OK).body("{\"ok\":true,\"status\":\"200\",\"service\":\"OnMind-XDB\",\"version\":\"$version\",\"driver\":\"$driver\",\"embedded\":${Rote.embedded}}")
            .header("Content-Type", "application/json")
    }

    private fun handleCoherenceStats(): (Request) -> Response = { _: Request ->
        try {
            val coherenceStats = CoherenceStore.getCoherenceStats()
            Response(OK).body(json.writeValueAsString(coherenceStats))
                .header("Content-Type", "application/json")
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).body("{\"error\":\"${e.message}\"}")
                .header("Content-Type", "application/json")
        }
    }

    private fun handleCoherenceVerify(): (Request) -> Response = { _: Request ->
        try {
            val isCoherent = CoherenceStore.verifyCoherence()
            val result = mapOf(
                "coherent" to isCoherent,
                "message" to if (isCoherent) "Data coherence verified successfully" else "Data coherence issues detected",
                "timestamp" to System.currentTimeMillis()
            )
            Response(OK).body(json.writeValueAsString(result))
                .header("Content-Type", "application/json")
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).body("{\"error\":\"${e.message}\"}")
                .header("Content-Type", "application/json")
        }
    }

    private fun handleCoherenceSync(): (Request) -> Response = { _: Request ->
        try {
            val success = CoherenceStore.forceSyncFromDisk()
            val result = mapOf(
                "success" to success,
                "message" to if (success) "Force sync completed successfully" else "Force sync failed",
                "timestamp" to System.currentTimeMillis()
            )
            val status = if (success) OK else Status.INTERNAL_SERVER_ERROR
            Response(status).body(json.writeValueAsString(result))
                .header("Content-Type", "application/json")
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).body("{\"error\":\"${e.message}\"}")
                .header("Content-Type", "application/json")
        }
    }

    private fun handleHealth(): (Request) -> Response = { _: Request ->
        try {
            val healthCheck = CoherenceStore.healthCheck()
            Response(OK).body(json.writeValueAsString(healthCheck))
                .header("Content-Type", "application/json")
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).body("{\"error\":\"${e.message}\"}")
                .header("Content-Type", "application/json")
        }
    }

    private fun handleTraceStats(): (Request) -> Response = { _: Request ->
        try {
            val traceStats = Trace.getStats()
            Response(OK).body(json.writeValueAsString(traceStats))
                .header("Content-Type", "application/json")
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).body("{\"error\":\"${e.message}\"}")
                .header("Content-Type", "application/json")
        }
    }
}
