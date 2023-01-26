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
import java.lang.Exception

object Rote {
    val os: String = System.getProperty("os.name")
    val home: String = System.getProperty("user.home")
    val separator: String = System.getProperty("file.separator")
    val fileName = "onmind.ini"
    var file: String = "..$separator$fileName"
    var path: String = ""
    var port = 9000

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
                    // 3. Verificar archivo de configuracion en directorio 'xy' de 'onmind'
                    if (os.contains("Windows", true))
                        file = "C:${separator}onmind${separator}xy${separator}$fileName"
                    else
                        file = "$home${separator}onmind${separator}xy${separator}$fileName"

                    if (!File(file).isFile()) {
                        // 4. Verificar archivo de configuracion en directorio 'xy' de 'onminder'
                        if (os.contains("Windows", true))
                            file = "C:${separator}onminder${separator}xy${separator}$fileName"
                        else
                            file = "$home${separator}onminder${separator}xy${separator}$fileName"

                        if (!File(file).isFile()) {
                            // 5. Verificar archivo de configuracion en directorio del usuario
                            if (os.contains("Windows", true))
                                file = "C:$separator$home$separator$fileName"
                            else
                                file = "$home$separator$fileName"

                            if (!File(file).isFile()) {
                                // 6. Verificar archivo de configuracion en directorio opcional 'onminder'
                                if (os.contains("Windows", true))
                                    file = "D:${separator}onminder${separator}xy${separator}$fileName"
                                else
                                    file = "${separator}opt${separator}onminder${separator}xy${separator}$fileName"

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
        path = config!!.getProperty("app.local")
        if (path == "C:/Users/home/" || path == "/home/user/") {
            path = "$home/onmind/"
            println("Preparing 'onmind' folder: $path ...")
            if (!File(path).exists()) {
                File(path).mkdir()
                File(path + "xy").mkdir()
            }
            else if (!File(path + "xy").exists())
                File(path + "xy").mkdir()
        }
        path += "xy/"
        if (os.contains("Windows"))
            path = path.replace("/", "\\")

        //if (driver == "5") {  // SQLite (embedded)
            driver = "org.sqlite.JDBC"
            boxUrl = "jdbc:sqlite:${path}xybox.db"
        //}
        //else if (driver == "2") {  // MariaDB / MySQL
            //driver = "org.mariadb.jdbc.Driver"
            //boxUrl = "jdbc:mariadb://$dbHost:$dbPort/xybox"
            //user = config!!.getProperty("db.user")
            //password = config!!.getProperty("db.password")
        //}

        print("Starting db connection ... ")
        val box: Connection?
        try {
            //println(boxUrl)
            box = DriverManager.getConnection(boxUrl, user, password)
            println("[  OK!  ]")
        }
        catch (e: SQLException) {
            println("[ ERROR ] ${e.message}")
            throw e
        }

        return box
    }

    fun index() = """<!doctype html>
        <html><head><title>OnMind | Platform</title><link rel="shortcut icon" href="server/view/favicon.ico">
        <style>body { font-family: 'Lucida Console', 'Courier New', Helvetica, Arial, sans-serif; color: gainsboro; background: black; }</style></head>
        <body><p>[ Hi, this is OK! ]> <span id="hi">_</span></p></body></html>
        """.trimIndent()

    fun helloWorld(name: String? = null) = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>Hello</title>
    </head>
    <body>
        <h1>Hello ${name ?: "there!"}</h1>
    </body>
    </html> 
    """
}
