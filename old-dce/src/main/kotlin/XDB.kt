import io.agroal.api.AgroalDataSource
import io.javalin.Javalin
import co.onmind.Rote
import co.onmind.AbcAPI

object onminddai {
    val os = System.getProperty("os.name")
    var dbc: AgroalDataSource? = null
    val driver = "org.h2.Driver"  // "org.monetdb.monetdbe.MonetDriver"
    var queryLimit = 1200
}

fun main() {  //args: Array<String>
    val filex = Rote.getConfigFile()
    val config = Rote.getConfig(filex)
    onminddai.dbc = Rote.getDB(config)
    val port = Rote.port
    val abc = AbcAPI()

    print("Starting web service ..... ")
    val app = Javalin.create() {
        it.enableCorsForAllOrigins()
        it.showJavalinBanner = false
    }.apply {
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("not found") }
    }.start(port)

    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop()
        if (onminddai.dbc != null)
            onminddai.dbc!!.close()
        println("[bye!]")
    })

    app.get("/") { ctx -> ctx.html(Rote.index()) }
    //app.get("/abc", abc.useControl)
    app.post("/abc", abc.useControl)
    println("[ OK ]")  //  http://127.0.0.1:$port
}
