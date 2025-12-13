/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 4/12/20.
 */

package co.onmind.util

import onmindxdb
import io.agroal.api.AgroalDataSource
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier
import io.agroal.api.security.NamePrincipal
import io.agroal.api.security.SimplePassword
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import java.time.Duration


object Rote {
    val os: String = System.getProperty("os.name")
    var home: String = System.getProperty("user.home")
    val separator: String = System.getProperty("file.separator")
    val fileName = "onmind.ini"
    var file: String = "..$separator$fileName"
    var path: String = ""
    var port = 9990
    var embedded = false

    fun getConfigFile(): String {
        try {
            // 1. Verificar archivo de configuracion en directorio inmediatamente anterior
            if (!File(file).isFile()) {
                // 2. Verificar archivo de configuracion en directorio del usuario y subdirectorio 'onmind'
                if (os.contains("Windows", true)) {
                    home = home.replace("\\Users\\", "/Users/")
                    file = "$home/onmind/$fileName"
                }
                else
                    file = "$home${separator}onmind${separator}$fileName"

                if (!File(file).isFile()) {
                    // 7. Verificar archivo de configuracion en el mismo directorio
                    val file2 = fileName

                    if (!File(file2).isFile()) {
                        // 8. No encontrado
                        // println("Not found: $fileName")
                        // System.exit(1)
                        val text = """
                            # Parametros del servicio frontal para aplicaciones web
                            app.mode = production
                            app.local = ${file.replace(fileName,"")}
                            app.base = /app
                            app.language = en
                            app.modality = 5
                            app.deploy = 0
                            app.ui = +

                            # Parametros del servicio adaptador de datos
                            dai.deploy = xdb
                            dai.port = 9990
                            dai.host = http://localhost
                            dai.cors = *

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

                            # Parametros de autenticacion
                            auth.enabled = true
                            auth.type = BASIC
                            auth.basic.user = admin
                            auth.basic.pass = admin

                            # Parametros de persistencia
                            kv.store = mvstore
                            kv.mvstore.name = xybox
                            kv.ehcache.name = xybox
                            kv.ehcache.max_entries = 10000
                            kv.dynamodb.table = onmind-xdb
                            kv.dynamodb.region = us-east-1
                        """.trimIndent()
                        try {
                            File(file.replace(fileName,"")).mkdir()
                            File(file).writeText(text)
                            File(file.replace(fileName,"xy")).mkdir()
                        }
                        catch (io: Exception) {
                            println("Not resolved: $file => $fileName")
                            System.exit(1)
                        }
                    }
                }
            }

            println("$os $file --> Checked OK!")
        }
        catch(e: Exception) {
            println("$os $file --> Check any malformed back-slash or change it by slash (/)")
            throw e
        }
        
        return file
    }

    fun getConfig(file: String) = Properties().apply {
        FileInputStream(file).use { fis ->
            load(fis)
        }
    }
    
    fun getDataSource(config: Properties): AgroalDataSource {
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
            } else if (!File(path + "xy").exists())
                File(path + "xy").mkdir()
        } else
            println("\nGetting 'onmind' folder ... [  OK!  ] => $path")

        path += "xy/"
        if (os.contains("Windows"))
            path = path.replace("/", "\\")

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
            val dataSourceConfig = AgroalDataSourceConfigurationSupplier()
                .connectionPoolConfiguration { cp ->
                    cp.maxSize(maxPoolSize)
                        .minSize(2)
                        .initialSize(2)
                        .acquisitionTimeout(Duration.ofSeconds(5))
                        .connectionFactoryConfiguration { cf ->
                            cf.jdbcUrl(boxUrl)
                                .connectionProviderClass(Class.forName(driver))
                                .principal(NamePrincipal(user))
                                .credential(SimplePassword(password))
                        }
                }

            val dataSource = AgroalDataSource.from(dataSourceConfig)
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
        //val dbName = config.getProperty("db.name")
        var boxUrl: String = ""
        var user: String = ""                                                         //config.getProperty("db.user")
        var password: String = ""                                                     //config.getProperty("db.password")

        //val javaRun = config.getProperty("app.java")
        val charset = config.getProperty("db.charset")
        var driver = config.getProperty("db.driver")
        path = config.getProperty("app.local")

        if (path == "C:/Users/home/" || path == "/home/user/" || path == "/Users/home/") {
            path = "$home/onmind/"
            println("\nPreparing 'onmind' folder.. [  OK!  ] => $path")
            if (!File(path).exists()) {
                File(path).mkdir()
                File(path + "xy").mkdir()
            }
            else if (!File(path + "xy").exists())
                File(path + "xy").mkdir()
        }
        else
            println("\nGetting 'onmind' folder ... [  OK!  ] => $path")

        path += "xy/"
        if (os.contains("Windows"))
            path = path.replace("/", "\\")

        if /*(driver == "5") {  // HerdDB (local)
            dbPort = "7000"
            driver = "herddb.jdbc.Driver"
            boxUrl = "jdbc:herddb:server:localhost:$dbPort"
        }
        else if*/ (driver == "6") {  // DuckDB (in-memory-embedded)
            embedded = true
            driver = "org.duckdb.DuckDBDriver"
            boxUrl = "jdbc:duckdb:"
        }
        else {  // H2 (in-memory-embedded) | Driver => 0
            embedded = true
            driver = "org.h2.Driver"
            boxUrl = "jdbc:h2:mem:xybox;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE"
        }

        print("Opening file/connection ... ")
        onmindxdb.driver = driver
        val box: Connection?
        try {
            if (driver.contains("h2"))
                box = DriverManager.getConnection(boxUrl, user, password)
            else {
                Class.forName(driver)
                box = DriverManager.getConnection(boxUrl)
            }
            println("[  OK!  ] => ${Timestamp(System.currentTimeMillis())}")
        }
        catch (e: SQLException) {
            println("[ ERROR ] ${e.message}")
            throw e
        }

        return box
    }
    
    fun getConnection(dataSource: AgroalDataSource): Connection = dataSource.connection
    
    fun isUIEnabled(config: Properties): Boolean {
        return config.getProperty("app.ui", "+") == "+"
    }

    fun welcome() = """<!doctype html>
        <html><head><title>OnMind-XDB</title>
        <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
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
                <!-- <div class="icon">🗄️</div> -->
                <h1>OnMind-XDB</h1>
                <p>eXpress DataBase WebApp</p>
                <p style="margin-top: 2rem;">Admin UI is disabled for this environment</p>
                <p style="font-size: 1rem; opacity: 0.8;">API service is running normally</p>
                <div class="version">v${onmindxdb.version}</div>
            </div>
        </body></html>
        """.trimIndent()
}
