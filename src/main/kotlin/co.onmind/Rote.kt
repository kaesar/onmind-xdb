/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 4/12/20.
 */

package co.onmind

import java.util.Properties
import java.io.File
import java.io.FileInputStream
import java.sql.DriverManager
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.lang.Exception

object Rote {
    val os: String = System.getProperty("os.name")
    val home: String = System.getProperty("user.home")
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
                if (os.contains("Windows", true))
                    file = "C:${separator}$home${separator}onmind${separator}$fileName"
                else
                    file = "$home${separator}onmind${separator}$fileName"

                if (!File(file).isFile()) {
                    // 7. Verificar archivo de configuracion en el mismo directorio
                    file = fileName

                    if (!File(file).isFile()) {
                        // 8. No encontrado
                        println("Not found: $fileName")
                        System.exit(1)
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

    fun getDB(config: Properties): Connection {
        port = (config.getProperty("dai.port") ?: "9000").toInt()
        val host = config.getProperty("dai.host")
        val dbHost = config.getProperty("db.host")
        var dbPort = config.getProperty("db.port")
        //val dbName = config.getProperty("db.name")
        var boxUrl: String = ""
        var user: String = ""                                                         //config.getProperty("db.user")
        var password: String = ""                                                     //config.getProperty("db.password")

        val javaRun = config.getProperty("app.java")
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

        //if (driver == "0") {  // H2 (embedded)
            embedded = true
            driver = "org.h2.Driver"
            boxUrl = "jdbc:h2:mem:xybox;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE"
        //} else
        //if (driver == "10") {  // MonetDB/e (local/embedded)
        //    driver = "org.monetdb.monetdbe.MonetDriver"
        //    boxUrl = "jdbc:monetdb:file:${path}xybox"
        //}
        //if (driver == "5") {  // SQLite (local/embedded)
            //driver = "org.sqlite.JDBC"
            //boxUrl = "jdbc:sqlite:${path}xybox.db"
        //}

        print("Opening file/connection ... ")
        val box: Connection?
        try {
            box = DriverManager.getConnection(boxUrl, user, password)
            println("[  OK!  ] => ${Timestamp(System.currentTimeMillis())}")
        }
        catch (e: SQLException) {
            println("[ ERROR ] ${e.message}")
            throw e
        }

        return box
    }

    fun index() = """<!doctype html>
        <html><head><title>OnMind | eXpress DataBase (XDB)</title><link rel="shortcut icon" href="server/view/favicon.ico">
        <style>body { font-family: 'Lucida Console', 'Courier New', Helvetica, Arial, sans-serif; color: gainsboro; background: black; }</style></head>
        <body><p>[ Hi, this is OK! ]> <span id="hi">_</span></p></body></html>
        """.trimIndent()
}
