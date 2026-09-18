import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.AnyOf
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
import com.zaxxer.hikari.HikariDataSource
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
import co.onmind.auth.AuthType
import co.onmind.auth.OTPMailPlug
import co.onmind.file.FileAPI
import co.onmind.file.FileMeta
import co.onmind.file.FileService
import co.onmind.file.FileStorage
import co.onmind.mcp.AbcMcpChat
import co.onmind.mcp.AbcMcpLlm
import co.onmind.mcp.AbcMcpServer
import co.onmind.mcp.AbcMcpTools

object onmindxdb {
    val os = System.getProperty("os.name")
    var dataSource: HikariDataSource? = null
    var dbc: Connection? = null
    var driver = "org.h2.Driver"
    var dbfile: String? = null
    var queryLimit = 1200
    var exportEnabled = false
    var config: Properties? = null
    val version = "0.9.0"
    var uiEnabled = true
    /** When true, UI shows a logout link (not NoAuth; only strategies with a real logout URL). */
    var uiShowLogout: Boolean = false
    var uiLogoutUrl: String = ""
    var fileEnabled = false
    var fileService: FileService? = null
    var fileTTL: java.time.Duration = java.time.Duration.ofHours(1)
    private val json = JsonMapper.instance

    @JvmStatic
    fun main(args: Array<String>) {
        val filex = Rote.getConfigFile()
        val cfg = Rote.getConfig(filex)
        config = cfg
        queryLimit = cfg.getProperty("db.query_limit", "1200").toIntOrNull() ?: 1200
        exportEnabled = cfg.getProperty("db.export", "-") == "+"
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
        
        // Hidratar contadores de coherencia tras carga inicial desde disco
        CoherenceStore.resyncCounters()

        val appMode = cfg.getProperty("app.mode", "production")
        val enableSwagger = appMode != "production"
        uiEnabled = cfg.getProperty("app.ui", "+") == "+"
        
        val authConfig = AuthConfig.fromConfig(cfg)
        val authProvider = authConfig.createProvider()
        val appLanguage = cfg.getProperty("app.language", "en")

        // Logout link: only when auth is enabled and the strategy has a real server-side logout.
        // NoAuth → hidden. BASIC → hidden (browser caches credentials; no clean logout).
        // OTPMAIL → /auth/otpmail/logout. OIDC/Cognito/Authelia → no in-app logout yet.
        val logoutUrl = if (authConfig.enabled) {
            when (authConfig.type) {
                AuthType.OTPMAIL -> "/auth/otpmail/logout"
                else -> null
            }
        } else null
        uiShowLogout = logoutUrl != null
        uiLogoutUrl = logoutUrl.orEmpty()

        // FILES feature: blob storage (S3/RustFS/MinIO) + metadata sheet FILES.
        fileEnabled = cfg.getProperty("file.enabled", "-") == "+"
        fileTTL = java.time.Duration.ofMinutes(
            cfg.getProperty("file.ttl", "60").toLongOrNull() ?: 60L
        )
        if (fileEnabled) {
            val endpoint = cfg.getProperty("s3.endpoint")?.trim()?.takeIf { it.isNotEmpty() }
            val bucket = cfg.getProperty("s3.bucket", "files")?.trim()?.ifEmpty { "files" } ?: "files"
            val region = cfg.getProperty("s3.region", "us-east-1")?.trim()?.ifEmpty { "us-east-1" } ?: "us-east-1"
            val accessKey = cfg.getProperty("s3.access_key")?.trim()?.takeIf { it.isNotEmpty() }
            val secretKey = cfg.getProperty("s3.secret_key")?.trim()?.takeIf { it.isNotEmpty() }
            val s3Service: FileService? = try {
                if (endpoint != null) {
                    // S3 backend lives in the full profile; reflect to keep lite green.
                    val clazz = Class.forName("co.onmind.file.S3FileService")
                    val ctor = clazz.getDeclaredConstructor(
                        String::class.java, String::class.java, String::class.java,
                        String::class.java, String::class.java
                    )
                    ctor.newInstance(bucket, endpoint, region, accessKey, secretKey) as FileService
                } else null
            } catch (e: Exception) {
                println("[WARN] S3 backend not available (${e.message}). Falling back to local file storage.")
                null
            }
            fileService = s3Service ?: FileStorage()
            FileMeta.ensureSheet("system")
        }
        
        val mcpEnabled = cfg.getProperty("mcp.enabled", "-") == "+"
        val mcpWrite = cfg.getProperty("mcp.write", "-") == "+"
        val mcpStdio = args.contains("--mcp") || cfg.getProperty("mcp.stdio", "-") == "+"
        val mcpLlm = cfg.getProperty("mcp.llm", "-")
        val mcpLlmUrl = cfg.getProperty("mcp.llm.url", "http://127.0.0.1:11434")
        val mcpLlmModel = cfg.getProperty("mcp.llm.model", "carstenuhlig/omnicoder-9b:latest")
        val mcpLlmTimeout = cfg.getProperty("mcp.llm.timeout", "120").toLongOrNull() ?: 120L

        val mcpTools = AbcMcpTools(abc, writeEnabled = mcpWrite)
        val mcpServer = if (mcpEnabled || mcpStdio) AbcMcpServer(mcpTools) else null
        val mcpLlmClient = if (mcpEnabled) {
            AbcMcpLlm.fromConfig(mcpTools, mcpLlm, mcpLlmUrl, mcpLlmModel, mcpLlmTimeout)
        } else null
        val mcpChat = if (mcpEnabled) AbcMcpChat(mcpTools, mcpLlmClient) else null

        // Optional gRPC on a dedicated Netty port (default 9991). Shares AbcAPI.dispatch core.
        // In lite profile, gRPC classes are not available - skip gracefully
        val grpcEnabled = try {
            args.contains("--grpc") || cfg.getProperty("grpc.enabled", "-") == "+"
        } catch (_: Exception) { false }
        val grpcPort = cfg.getProperty("grpc.port", "9991").toIntOrNull() ?: 9991
        var grpcServer: Any? = null  // Use Any? to avoid class not found in lite profile

        // MCP-only stdio mode: no Jetty (for Claude Desktop / Cursor / agent hosts).
        if (mcpStdio && mcpServer != null) {
            println("MCP stdio mode (read-only abc_* tools). Waiting for JSON-RPC on stdin...")
            mcpServer.serveStdio()
            return
        }

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
            "/favicon.ico" bind Method.GET to { _: Request -> 
                val stream = onmindxdb::class.java.getResourceAsStream("/static/favicon.ico")
                if (stream != null) Response(OK).body(stream).header("Content-Type", "image/x-icon")
                else Response(Status.NOT_FOUND)
            },
            "/static" bind GZip().then(static(Classpath("/static"))),
            appUI.routes()
        )

        if (mcpEnabled && mcpServer != null) {
            routesList.add("/mcp" bind Method.GET to mcpServer.httpHandler())
            routesList.add("/mcp" bind Method.POST to mcpServer.httpHandler())
        }

        if (mcpEnabled && mcpChat != null) {
            routesList.add("/mcp/chat" bind Method.GET to mcpChat.httpHandler())
            routesList.add("/mcp/chat" bind Method.POST to mcpChat.httpHandler())
        }

        // OTP Mail login/send/verify/logout (public paths; filter allows them without session)
        if (authProvider is OTPMailPlug) {
            routesList.add(authProvider.routes())
        }

        // FILES feature: /file REST API (registered only when file.enabled=+)
        fileService?.let { service ->
            routesList.add(FileAPI(service, fileTTL).routes())
        }

        if (enableSwagger) {
            routesList.add("/swagger" bind Method.GET to { _: Request -> Response(OK).body(Swagger.ui()).header("Content-Type", "text/html") })
        }
        
        // Paths that never require authentication (health, static assets, root).
        val publicPaths = setOf("/health", "/favicon.ico")
        val publicPrefixes = listOf("/static")

        val app = RequestTracing()
            .then(ResponseFilters.ReportHttpTransaction { tx ->
                val logMsg = "[${tx.request.method}] ${tx.request.uri} -> ${tx.response.status.code}"
                // Usar el nuevo sistema de trace optimizado
                Trace.logRequest(tx.request.method.toString(), tx.request.uri.toString(), tx.response.status.code)
                
                // Fallback a println si trace está desactivado
                if (CoherenceConfig.logLevel == 0)
                    println(logMsg)
            })
            // CORS fuera del filtro auth: el propio filtro Cors responde 200 al preflight
            // OPTIONS y añade cabeceras a TODAS las respuestas (incluidos los 401), en vez
            // de que el navegador los enmascare como errores CORS.
            // dai.cors = * (eXpress default) -> AllowAll; o lista separada por comas.
            .then(Cors(buildCorsPolicy()))
            .then(Filter { next -> { request ->
                val path = request.uri.path
                val isPublic = path in publicPaths || publicPrefixes.any { path.startsWith(it) }
                if (isPublic) next(request) else authProvider.filter().invoke(next)(request)
            }})
            .then(routes(*routesList.toTypedArray()))

        if (mcpEnabled) {
            val toolsLine = if (mcpWrite) "abc_status, abc_list, abc_describe, abc_find, abc_create, abc_define, abc_schema" else "abc_status, abc_list, abc_describe, abc_find"
            val mode = if (mcpWrite) "read + schema-write" else "read-only"
            val llmLine = if (mcpLlmClient != null) {
                "MCP llm => ${mcpLlmClient.info()["model"]} @ ${mcpLlmClient.info()["url"]}"
            } else {
                "MCP llm => off (set mcp.llm=ollama for local model chat)"
            }
            println("[  OK!  ] => http://127.0.0.1:${port}\n")
            println("MCP $mode => http://127.0.0.1:${port}/mcp")
            println("MCP chat => http://127.0.0.1:${port}/mcp/chat  (tools: $toolsLine)")
            println("$llmLine\n")
        } else {
            println("[  OK!  ] => http://127.0.0.1:${port}\n")
        }

        if (fileEnabled) {
            val mode = if (fileService is FileStorage) "local" else "s3"
            val bucket = fileService?.bucket ?: "-"
            println("FILES feature => /file (mode: $mode, bucket: $bucket, UI: /app/files)")
        }

        if (grpcEnabled) {
            try {
                // Use reflection to avoid class not found in lite profile
                val grpcClass = Class.forName("co.onmind.grpc.AbcGrpcServer")
                val constructor = grpcClass.getDeclaredConstructor(Int::class.java, AbcAPI::class.java)
                grpcServer = constructor.newInstance(grpcPort, abc)
                grpcServer?.let { server ->
                    val startMethod = server.javaClass.getMethod("start")
                    startMethod.invoke(server)
                }
            } catch (e: ClassNotFoundException) {
                println("[WARN] gRPC classes not available (lite profile). Skipping gRPC server.")
            } catch (e: Exception) {
                println("[ERROR] Failed to start gRPC server: ${e.message}")
            }
        }

        val serve = app.asServer(Jetty(port)).start()

        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                grpcServer?.let { server ->
                    val stopMethod = server.javaClass.getMethod("stop")
                    stopMethod.invoke(server)
                }
            } catch (_: Exception) {
            }
            fileService?.close()
            Trace.shutdown()
        })

        serve.block()
    }

    private fun buildCorsPolicy(): CorsPolicy {
        val origins = config?.let { Rote.corsOrigins(it) }
        val originPolicy = if (origins == null) {
            println("CORS => AllowAll (eXpress default, dai.cors=*)")
            OriginPolicy.AllowAll()
        } else {
            println("CORS => restricted to ${origins.joinToString(", ")}")
            OriginPolicy.AnyOf(origins)
        }
        return CorsPolicy(
            originPolicy,
            listOf("Content-Type", "Cache-Control", "X-Request-Id", "Authorization"),
            listOf(Method.POST, Method.GET, Method.PUT, Method.DELETE, Method.PATCH)
        )
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
