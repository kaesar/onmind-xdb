/** Created by Cesar Andres Arcila Buitrago from Colombia on 4/12/20. */
package co.onmind.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Duration
import java.util.*
import onmindxdb

object Rote {
    val os: String = System.getProperty("os.name")
    var home: String = System.getProperty("user.home")
    val separator: String = System.getProperty("file.separator")
    val fileName = "onmind.ini"
    var file: String = "..$separator$fileName"
    var path: String = ""
    var port = 9990
    var embedded = false

    /** Resolve dots and `..` segments in the app.local path. */
    fun normalize(path: String): String {
        val cleaned = path.replace("\\", "/").removeSuffix("/")
        val parts = cleaned.split("/").filter { it.isNotEmpty() && it != "." }
        val stack = ArrayDeque<String>()
        for (p in parts) {
            if (p == "..") {
                if (stack.isNotEmpty()) stack.removeLast()
            } else stack.addLast(p)
        }
        return stack.joinToString("/")
    }

    fun getConfigFile(): String {
        try {
            // 1. Verificar archivo de configuracion en directorio inmediatamente anterior
            if (!File(file).isFile()) {
                // 2. Verificar archivo de configuracion en directorio del usuario y subdirectorio
                // 'onmind'
                if (os.contains("Windows", true)) {
                    home = home.replace("\\Users\\", "/Users/")
                    file = "$home/onmind/$fileName"
                } else {
                    file = "$home${separator}onmind${separator}$fileName"
                    // 3. Linux container fallback: check /app/onmind.ini (used with -w /app + bind mount)
                    if (os.contains("Linux", true) && !File(file).isFile()) {
                        val containerFile = "/app${separator}$fileName"
                        if (File(containerFile).isFile()) {
                            file = containerFile
                        }
                    }
                }

                if (!File(file).isFile()) {
                    // 7. Verificar archivo de configuracion en el mismo directorio
                    val file2 = fileName

                    if (!File(file2).isFile()) {
                        // 8. No encontrado
                        // println("Not found: $fileName")
                        // System.exit(1)
                        val text =
                                """
                            # Parametros del servicio frontal para aplicaciones web
                            app.mode = production
                            app.local = ${file.replace(fileName,"")}
                            app.base = /app
                            app.language = en
                            app.logger = -
                            app.modality = 5
                            app.deploy = 0
                            app.ui = +

                            # Parametros del servicio adaptador de datos
                            dai.deploy = xdb
                            dai.port = 9990
                            dai.host = http://localhost
                            dai.cors = *

                            # MCP (Model Context Protocol) — abc_* tools over /mcp (and /mcp/chat)
                            # mcp.enabled = +   # expose HTTP JSON-RPC at /mcp + chat panel in Dashboard
                            # mcp.stdio = +     # or pass --mcp for stdio-only agent hosts (Claude/Cursor)
                            # mcp.write = +     # enables abc_create, abc_define, abc_schema (schema only, not row writes)
                            # Local LLM for Dashboard chat (Ollama). Example model: carstenuhlig/omnicoder-9b:latest
                            # mcp.llm = ollama
                            # mcp.llm.url = http://127.0.0.1:11434
                            # mcp.llm.model = carstenuhlig/omnicoder-9b:latest
                            # mcp.llm.timeout = 120
                            mcp.enabled = -
                            mcp.stdio = -
                            mcp.write = -
                            mcp.llm = -
                            mcp.llm.url = http://127.0.0.1:11434
                            mcp.llm.model = carstenuhlig/omnicoder-9b:latest
                            mcp.llm.timeout = 120

                            # gRPC (Protocol Buffers) — ABC mirror over Netty on a dedicated port
                            # Reuses the same ABC core (AbcAPI.dispatch) as /abc and /mcp.
                            # grpc.enabled = +   # expose localhost:<grpc.port> AbcService (Execute, Status)
                            grpc.enabled = -
                            grpc.port = 9991

                            # Parametros conector de base de datos
                            db.driver = 0
                            db.port = 9091
                            db.host = localhost
                            db.name = xe
                            db.user = xy
                            db.password = password
                            db.max_pool_size = 10
                            db.query_limit = 1200
                            db.charset = UTF-8
                            db.export = -      # + enables /abc what=export to SQLite file

                            # Parametros de autenticacion
                            auth.enabled = true
                            auth.type = BASIC
                            auth.basic.user = YWRtaW4=
                            auth.basic.pass = YWRtaW4=
                            
                            # FILES feature — blob storage (RustFS/MinIO/AWS S3)
                            # file.enabled = + activates /file API, FILES sheet and /app/files UI.
                            # With s3.endpoint set, presigned URLs point to that S3-compatible
                            # service; without it, blobs live under <app.local>/xy/files (local mode).
                            file.enabled = -
                            file.ttl = 60
                            s3.endpoint = http://rustfs:9000
                            s3.region = us-east-1
                            s3.bucket = files
                            s3.access_key =
                            s3.secret_key =
                            
                            # OIDC / Keycloak / EntraID configuration
                            auth.oidc.url = http://localhost:8080
                            auth.oidc.realm = master
                            auth.oidc.client_id = onmind-xdb
                            # auth.oidc.user_claim = sub
                            # auth.oidc.roles_claim = roles

                            # OTP Mail (passwordless) — set auth.type = OTPMAIL to enable
                            # auth.otp.smtp_host = localhost
                            # auth.otp.smtp_port = 1025
                            # auth.otp.smtp_user =
                            # auth.otp.smtp_pass =
                            # auth.otp.from = xdb@localhost
                            # auth.otp.session_key = change-me-otp-session-key
                            # auth.otp.auto_register = true

                            # Parametros de persistencia
                            kv.store = mvstore
                            kv.mvstore.name = xybox
                            kv.dynamodb.table = onmind-xdb
                            kv.dynamodb.region = us-east-1
                        """.trimIndent()
                        try {
                            File(file.replace(fileName, "")).mkdir()
                            File(file).writeText(text)
                            File(file.replace(fileName, "xy")).mkdir()
                        } catch (io: Exception) {
                            println("Not resolved: $file => $fileName")
                            System.exit(1)
                        }
                    }
                }
            }

            println("$os $file --> Checked OK!")
        } catch (e: Exception) {
            println("$os $file --> Check any malformed back-slash or change it by slash (/)")
            throw e
        }

        return file
    }

    fun getConfig(file: String) =
            Properties().apply { FileInputStream(file).use { fis -> load(fis) } }

    fun getDataSource(config: Properties): HikariDataSource {
        port = (config.getProperty("dai.port") ?: "9000").toInt()
        val maxPoolSize = (config.getProperty("db.max_pool_size") ?: "10").toInt()
        path = config.getProperty("app.local")
        var driver = config.getProperty("db.driver")
        var boxUrl: String
        var user = ""
        var password = ""

        if (path == "C:/Users/home/" || path == "/home/user/" || path == "/Users/home/") {
            path = "$home/onmind/"
            println("\nPreparing 'onmind' folder.. [  OK!  ] => $path")
            if (!File(path).exists()) {
                File(path).mkdir()
                File(path + "xy").mkdir()
            } else if (!File(path + "xy").exists()) File(path + "xy").mkdir()
        } else println("\nGetting 'onmind' folder ... [  OK!  ] => $path")

        path = normalize(path) + "/xy/"
        if (os.contains("Windows")) path = path.replace("/", "\\")

        if (driver == "6") {
            embedded = true
            driver = "org.duckdb.DuckDBDriver"
            boxUrl = "jdbc:duckdb:"
        } else {
            embedded = true
            driver = "org.h2.Driver"
            boxUrl = "jdbc:h2:mem:xybox;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE"
        }

        print("Opening file/connection ... ")
        onmindxdb.driver = driver

        try {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = boxUrl
                driverClassName = driver
                username = user
                password = password
                maximumPoolSize = maxPoolSize
                minimumIdle = 2
                connectionTimeout = Duration.ofSeconds(5).toMillis()
                poolName = "XDB-Pool"
            }

            val dataSource = HikariDataSource(hikariConfig)
            println("[  OK!  ] => ${Timestamp(System.currentTimeMillis())}")
            return dataSource
        } catch (e: SQLException) {
            println("[ ERROR ] ${e.message}")
            throw e
        }
    }

    @Deprecated("Use getDataSource instead")
    fun getDB(config: Properties): Connection {
        port = (config.getProperty("dai.port") ?: "9000").toInt()
        val host = config.getProperty("dai.host")
        val dbHost = config.getProperty("db.host")
        var dbPort = config.getProperty("db.port")
        // val dbName = config.getProperty("db.name")
        var boxUrl: String = ""
        var user: String = "" // config.getProperty("db.user")
        var password: String = "" // config.getProperty("db.password")

        // val javaRun = config.getProperty("app.java")
        val charset = config.getProperty("db.charset")
        var driver = config.getProperty("db.driver")
        path = config.getProperty("app.local")

        if (path == "C:/Users/home/" || path == "/home/user/" || path == "/Users/home/") {
            path = "$home/onmind/"
            println("\nPreparing 'onmind' folder.. [  OK!  ] => $path")
            if (!File(path).exists()) {
                File(path).mkdir()
                File(path + "xy").mkdir()
            } else if (!File(path + "xy").exists()) File(path + "xy").mkdir()
        } else println("\nGetting 'onmind' folder ... [  OK!  ] => $path")

        path += "xy/"
        if (os.contains("Windows")) path = path.replace("/", "\\")

        if (driver == "6") { // DuckDB (in-memory-embedded)
            embedded = true
            driver = "org.duckdb.DuckDBDriver"
            boxUrl = "jdbc:duckdb:"
        } else { // H2 (in-memory-embedded) | Driver => 0
            embedded = true
            driver = "org.h2.Driver"
            boxUrl = "jdbc:h2:mem:xybox;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE"
        }

        print("Opening file/connection ... ")
        onmindxdb.driver = driver
        val box: Connection?
        try {
            if (driver.contains("h2")) box = DriverManager.getConnection(boxUrl, user, password)
            else {
                Class.forName(driver)
                box = DriverManager.getConnection(boxUrl)
            }
            println("[  OK!  ] => ${Timestamp(System.currentTimeMillis())}")
        } catch (e: SQLException) {
            println("[ ERROR ] ${e.message}")
            throw e
        }

        return box
    }

    fun getConnection(dataSource: HikariDataSource): Connection = dataSource.connection

    fun isUIEnabled(config: Properties): Boolean {
        return config.getProperty("app.ui", "+") == "+"
    }

    fun welcome() =
            """<!doctype html>
        <html><head><title>OnMind-XDB</title>
        <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
        <style>
            body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; 
                   background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }
            .container { text-align: center; color: white; padding: 2rem; }
            h1 { font-size: 3rem; margin: 0 0 1rem 0; font-weight: 700; }
            p { font-size: 1.25rem; margin: 0.5rem 0; opacity: 0.9; }
            .version { font-size: 0.875rem; margin-top: 2rem; opacity: 0.7; }
            .icon { font-size: 4rem; margin-bottom: 1rem; }
        </style></head>
        <body>
            <div class="container">

                <h1>OnMind-XDB</h1>
                <p>eXpress DataBase WebApp</p>
                <p style="margin-top: 2rem;">Admin UI is disabled for this environment</p>
                <p style="font-size: 1rem; opacity: 0.8;">API service is running normally</p>
                <div class="version">v${onmindxdb.version}</div>
            </div>
        </body></html>
        """.trimIndent()
}
