/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 4/12/20.
 */

package co.onmind

//import io.agroal.pool.ConnectionPool
import io.agroal.api.AgroalDataSource
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier
import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation.SERIALIZABLE
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator
import io.agroal.api.configuration.AgroalDataSourceConfiguration.DataSourceImplementation
import io.agroal.api.security.SimplePassword
import io.agroal.api.security.NamePrincipal
import java.util.Properties
import java.io.File
import java.io.FileInputStream
import java.time.Duration.ofSeconds
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

    fun getDB(config: Properties): AgroalDataSource {
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
            println("Preparing 'onmind' folder: $path ...")
            if (!File(path).exists()) {
                File(path).mkdir()
                File(path + "xy").mkdir()
            }
            else if (!File(path + "xy").exists())
                File(path + "xy").mkdir()
        }
        else
            println("Getting 'onmind' folder: $path ...")

        path += "xy/"
        if (os.contains("Windows"))
            path = path.replace("/", "\\")

        //if (driver == "0") {  // H2 (embedded)
            embedded = true
            //driver = "org.h2.Driver"
            //boxUrl = "jdbc:h2:${path}xybox;AUTO_SERVER=TRUE;IGNORECASE=TRUE"  //;MV_STORE=TRUE;MVCC=TRUE;COLLATION='ENGLISH'
            //user = "xyadmin"
            //password = "saDXL-540"
            //password = config.getProperty("app.local") + "xy/.xybox.txt saDXL-540"  // D0892875DB7861CF => salt for DXL-540
        //} else
        //if (driver == "10") {  // MonetDB/e (local/embedded)
        //    driver = "org.monetdb.monetdbe.MonetDriver"
        //    boxUrl = "jdbc:monetdb:file:${path}xybox"
        //}
        //if (driver == "5") {  // SQLite (local/embedded)
            driver = "org.sqlite.JDBC"
            boxUrl = "jdbc:sqlite:${path}xybox.db"
        //}
        
        val configuration = AgroalDataSourceConfigurationSupplier()
            .dataSourceImplementation(DataSourceImplementation.AGROAL)
            .metricsEnabled(false)
            .connectionPoolConfiguration { cp: AgroalConnectionPoolConfigurationSupplier -> cp
                .minSize(5)
                .maxSize(20)
                .initialSize(10)
                .connectionValidator(defaultValidator())
                .acquisitionTimeout(ofSeconds(5))
                .leakTimeout(ofSeconds(5))
                .validationTimeout(ofSeconds(50))
                .reapTimeout(ofSeconds(500))
                .connectionFactoryConfiguration { cf: AgroalConnectionFactoryConfigurationSupplier -> cf
                    .jdbcUrl(boxUrl)
                    .connectionProviderClassName(driver)  // onminddai.driver
                    .autoCommit(false)
                    .jdbcTransactionIsolation(SERIALIZABLE)
                    .principal(NamePrincipal(user))
                    .credential(SimplePassword(password))
                }
            }

        print("Starting db connection ... ")
        val ds: AgroalDataSource?
        try {
            ds = AgroalDataSource.from(configuration)
            println("[ OK ]")
            /*ds!!.use { dataSource ->
                val connection: Connection = dataSource.connection
                connection.close()
            }*/
        }
        catch (e: SQLException) {
            println("[ ERROR ] ${e.message}")
            throw e
        }
        return ds
    }

    fun index() = """<!doctype html>
        <html><head><title>OnMind | Platform</title><link rel="shortcut icon" href="server/view/favicon.ico">
        <style>body { font-family: 'Lucida Console', 'Courier New', Helvetica, Arial, sans-serif; color: gainsboro; background: black; }</style></head>
        <body><p>[ Hi, this is OK! ]> <span id="hi">_</span></p></body></html>
        """.trimIndent()
}
