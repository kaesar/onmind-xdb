import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters.Cors
import org.http4k.routing.bind
import org.http4k.routing.routes
//import org.http4k.routing.static
import org.http4k.server.SunHttp
import org.http4k.server.asServer
//import org.http4k.routing.ResourceLoader.Companion.Classpath
//import org.http4k.cloudnative.env.Environment
//import org.http4k.client.ApacheClient
import java.sql.Connection
import java.util.Properties
import co.onmind.util.Rote
import co.onmind.api.AbcAPI
import co.onmind.db.RDB

object onmindxdb {
    val os = System.getProperty("os.name")
    var dbc: Connection? = null  // AgroalDataSource? = null
    var driver = "org.h2.Driver"
    var dbfile: String? = null
    var queryLimit = 1200
    var config: Properties? = null

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
        val xdb = RDB()
        xdb.readPoint()

        print("Exposing api/db service ... ")
        val app = routes(
            "/" bind Method.GET to { _: Request -> Response(OK).body(Rote.index()) },
            "/abc" bind Method.GET to abc.useControl(),
            "/abc" bind Method.POST to abc.useControl()
        ).withFilter(Cors(CorsPolicy(
            OriginPolicy.AllowAll(),  // AnyOf(listOf("*"))
            listOf("Content-Type", "Cache-Control"),
            listOf(Method.POST, Method.GET)  // Method.values().toList()
        )))

        println("[  OK!  ] => http://127.0.0.1:${port}")
        val serve = app.asServer(SunHttp(port)).start()  // Netty is an alternative
        serve.block()
    }
}
